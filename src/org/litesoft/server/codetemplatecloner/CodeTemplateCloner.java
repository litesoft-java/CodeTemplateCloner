package org.litesoft.server.codetemplatecloner;

import org.litesoft.commonfoundation.annotations.*;
import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.typeutils.*;
import org.litesoft.server.file.*;

import java.io.*;
import java.util.*;

public class CodeTemplateCloner {
    protected static final String NOT_SIGNIFICANT = "NOT Significant - empty or just spaces";

    protected final String mTemplateString, mWhat;
    protected final Set<String> mFileExtensionsToProcess = Sets.newHashSet();

    public CodeTemplateCloner( String pTemplateString, String pWhat, String pPrimaryFileExtensionToProcess, String... pAdditionalFileExtensionToProcess ) {
        mTemplateString = Confirm.significant( "TemplateString", pTemplateString );
        mWhat = Confirm.significant( "What", pWhat );
        mFileExtensionsToProcess.add( validateExtension( pPrimaryFileExtensionToProcess ) );
        for ( String zExtensionToProcess : ConstrainTo.notNull( pAdditionalFileExtensionToProcess ) ) {
            mFileExtensionsToProcess.add( validateExtension( zExtensionToProcess ) );
        }
    }

    private String validateExtension( String pExtension ) {
        String zExtension = (pExtension = ConstrainTo.notNull( pExtension )).trim();
        if ( zExtension.length() == 0 ) {
            throw new IllegalArgumentException( "Empty Extensions (including nothing but spaces) are not supported" );
        }
        if ( !zExtension.equals( pExtension ) ) {
            throw new IllegalArgumentException( "Extensions with leading or trailing spaces are not supported" );
        }
        return pExtension;
    }

    protected int usage( String pProblem ) {
        System.out.flush();
        System.err.println( "Error: " + pProblem );
        System.err.println( "The arguments(s) are:" );
        System.err.println( "    '" + mWhat + "'" );
        System.err.println( "    followed by the optional directories to process (if none are specified then the current directory will be used)" );
        return 1;
    }

    private void check( String pProblem, boolean pTrueExpected ) {
        if ( !pTrueExpected ) {
            System.exit( usage( pProblem ) );
        }
    }

    public int process( String[] pArgs )
            throws Exception {
        check( "No Args", Currently.isNotNullOrEmpty( pArgs ) );
        String zReplacementString = ConstrainTo.significantOrNull( pArgs[0] );
        check( "1st Arg - " + NOT_SIGNIFICANT, Currently.isNotNull( zReplacementString ) );
        CanonicalDirectories zPaths = new CanonicalDirectories();
        if ( pArgs.length == 1 ) {
            Confirm.isNotNull( "Can't Canonicalize Current Working Directory", zPaths.add( FileUtils.currentWorkingDirectory() ) );
        } else {
            for ( int i = 1; i < pArgs.length; i++ ) {
                String zArgRef = Integers.toNth( i + 2 ) + " Arg ";
                String zArg = ConstrainTo.significantOrNull( pArgs[i] );
                check( zArgRef + "- " + NOT_SIGNIFICANT, Currently.significant( zArg ) );
                zArgRef += "'" + zArg + "' - ";
                File zFile = zPaths.add( zArg );
                check( zArgRef + "Can't Canonicalize as a Directory (invalid path or not a Directory)", Currently.isNotNull( zFile ) );
                check( zArgRef + "Directory not Readable & Writable", zFile.canRead() && zFile.canWrite() );
                check( zArgRef + "Directory (provided) may NOT contain '" + mTemplateString + "'", !zFile.getAbsolutePath().contains( mTemplateString ) );
            }
        }
        createProcessor( new Replacer( mTemplateString, zReplacementString ) ).process( zPaths.getUserPaths() );
        return 0;
    }

    public static CodeLineChanger insertBeforeIfStartsWith(Replacer pReplacer, String pStartsWith) {
        return new CodeLineChangerStartsWithInsertChangedBefore( pReplacer, pStartsWith );
    }

    protected Processor createProcessor( Replacer pReplacer ) {
        return new Processor( pReplacer );
    }

    protected class Processor {
        protected final Replacer mReplacer;
        private final CodeLineChanger mCodeLineChanger;
        protected final Set<String> mSkippedDirs;

        protected Processor( Replacer pReplacer, String[] pSkippedDirs, CodeLineChanger pCodeLineChanger ) {
            mReplacer = pReplacer;
            mCodeLineChanger = ConstrainTo.notNull( pCodeLineChanger, CodeLineChanger.NO_OP );
            mSkippedDirs = Sets.newHashSet( ConstrainTo.notNull( pSkippedDirs ) );
        }

        protected Processor( Replacer pReplacer, CodeLineChanger pCodeLineChanger ) {
            this(pReplacer, Strings.EMPTY_ARRAY, pCodeLineChanger );
        }

        protected Processor( Replacer pReplacer, String... pSkippedDirs ) {
            this( pReplacer, pSkippedDirs, null );
        }

        protected Processor( Replacer pReplacer ) {
            this( pReplacer, (CodeLineChanger)null );
        }

        public void process( List<String> pUniqueUserDirs ) {
            for ( String zDirectory : pUniqueUserDirs ) {
                process( zDirectory );
            }
        }

        public void process( String pUserDirPath ) {
            System.out.println( "  " + pUserDirPath );
            processDir( new File( pUserDirPath ) );
        }

        public void processDir( File pDir ) {
            processDirs( pDir, pDir.list( FileUtils.DIRECTORIES_ONLY ) );
            processFiles( pDir, pDir.list( FileUtils.FILES_ONLY ) );
        }

        protected void processDirs( File pDir, String[] pNames ) {
            for ( String zName : pNames ) {
                if ( shouldProcessDir( pDir, zName ) ) {
                    String zTransformedName = processDirName( pDir, zName );
                    if ( zTransformedName != null ) {
                        processDir( new File( pDir, zTransformedName ) );
                    }
                }
            }
        }

        @SuppressWarnings("UnusedParameters")
        protected boolean shouldProcessDir( File pDir, String pName ) {
            return !mSkippedDirs.contains( pName );
        }

        protected String processDirName( File pDir, String pName ) {
            String zTransformedName = mReplacer.apply( pName );
            if ( !zTransformedName.equals( pName ) ) {
                File zSourceFile = new File( pDir, pName );
                File zDestinationFile = new File( pDir, zTransformedName );
                FileUtils.renameFromTo( zSourceFile, zDestinationFile );
                System.out.println( "    Dir: " + zSourceFile + " -> " + zDestinationFile );
            }
            return zTransformedName;
        }

        protected void processFiles( File pDir, String[] pNames ) {
            for ( String zName : pNames ) {
                processFile( new File( pDir, processFileName( pDir, zName ) ) );
            }
        }

        protected String processFileName( File pDir, String pName ) {
            String zTransformedName = mReplacer.apply( pName );
            if ( !zTransformedName.equals( pName ) ) {
                File zSourceFile = new File( pDir, pName );
                File zDestinationFile = new File( pDir, zTransformedName );
                FileUtils.renameFromTo( zSourceFile, zDestinationFile );
                System.out.println( "    File: " + zSourceFile + " -> " + zDestinationFile );
            }
            return zTransformedName;
        }

        protected void processFile( File pFile ) {
            if ( shouldProcessFileContents( pFile, FileUtils.getExtension( pFile ) ) ) {
                processFileContents( pFile );
            }
        }

        /**
         * Override this this method if you need to support text processing of individual files, like a file w/ NO Extension.
         */
        @SuppressWarnings("UnusedParameters")
        protected boolean shouldProcessFileContents( @NotNull File pFile, @NotNull String pExtension ) {
            return mFileExtensionsToProcess.contains( pExtension );
        }

        protected void processFileContents( @NotNull File pFile ) {
            String[] zNewLines = produceUpdatedFileContents( FileUtils.loadTextFile( pFile ) );
            if ( zNewLines != null ) {
                FileUtils.Change zChange = FileUtils.storeTextFile( pFile, zNewLines );
                if ( zChange != null ) {
                    System.out.print( "          " + pFile );
                    FileUtils.report( zChange, System.out );
                    System.out.println();
                    if ( zChange == FileUtils.Change.Updated ) {
                        FileUtils.deleteIfExists( FileUtils.asBackupFile( pFile ) );
                    }
                }
            }
        }

        /**
         * Produce the updated File Contents OR Null if there are no indicated changes.
         */
        protected String[] produceUpdatedFileContents( @NotNull String[] pLines ) {
            boolean zChanged = false;
            List<String> zCollector = Lists.newArrayList();
            for ( String zLine : pLines ) {
                zChanged |= processFileLine( zCollector, zLine );
            }
            return !zChanged ? null // Indicating NO Change
                             : zCollector.toArray( new String[zCollector.size()] );
        }

        private boolean processFileLine( List<String> pCollector, String pLine ) {
            if ( mReplacer.willChange( pLine ) ) {
                return processFileLineShouldChange( pCollector, pLine );
            }
            pCollector.add( pLine );
            return false;
        }

        private boolean processFileLineShouldChange( List<String> pCollector, String pLine ) {
            String zChangedLine = mCodeLineChanger.changeLine( pCollector, pLine );
            if ( !pLine.equals( zChangedLine ) ) {
                if ( zChangedLine != null ) {
                    pCollector.add( zChangedLine );
                }
                return true;
            }
            zChangedLine = mReplacer.apply( pLine );
            if ( !pLine.equals( zChangedLine ) ) {
                pCollector.add( zChangedLine );
                return true;
            }
            return false;
        }
    }
}
