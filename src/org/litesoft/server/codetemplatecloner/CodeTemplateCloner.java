package org.litesoft.server.codetemplatecloner;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.typeutils.*;
import org.litesoft.server.file.*;

import java.io.*;
import java.util.*;

public class CodeTemplateCloner {
    protected static final String NOT_SIGNIFICANT = "NOT Significant - empty or just spaces";

    protected final String mTemplateString, mWhat;

    public CodeTemplateCloner( String pTemplateString, String pWhat ) {
        mTemplateString = Confirm.significant( "TemplateString", pTemplateString );
        mWhat = Confirm.significant( "What", pWhat );
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
        createProcessor( zReplacementString ).process( zPaths.getUserPaths() );
        return 0;
    }

    protected Processor createProcessor( String pReplacementString ) {
        return new Processor( pReplacementString );
    }

    protected class Processor {
        protected final String mTemplateStringLC, mReplacementString, mReplacementStringLC;
        private final CodeLineChanger mCodeLineChanger;

        public Processor( String pReplacementString, CodeLineChanger pCodeLineChanger ) {
            mTemplateStringLC = mTemplateString.toLowerCase();
            mReplacementStringLC = (mReplacementString = pReplacementString).toLowerCase();
            System.out.println( "    Replace: '" + mTemplateString + "' with '" + mReplacementString + "'" );
            System.out.println( "        and: '" + mTemplateStringLC + "' with '" + mReplacementStringLC + "'" );
            mCodeLineChanger = ConstrainTo.notNull( pCodeLineChanger, CodeLineChanger.NO_OP );
        }

        public Processor( String pReplacementString ) {
            this(pReplacementString, null);
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
            String[] zEntries = pDir.list();

            // TODO: XXX
        }
    }
}
