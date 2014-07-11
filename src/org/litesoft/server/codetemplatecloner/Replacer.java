package org.litesoft.server.codetemplatecloner;

public class Replacer {
    private final String mTemplateString, mTemplateStringLC, mReplacementString, mReplacementStringLC;

    public Replacer( String pTemplateString, String pReplacementString ) {
        mTemplateStringLC = (mTemplateString = pTemplateString).toLowerCase();
        mReplacementStringLC = (mReplacementString = pReplacementString).toLowerCase();
        System.out.println( "    Replace: '" + mTemplateString + "' with '" + mReplacementString + "'" );
        System.out.println( "        and: '" + mTemplateStringLC + "' with '" + mReplacementStringLC + "'" );
    }

    public String getTemplateStringLC() {
        return mTemplateStringLC;
    }

    public boolean willChange( String pText ) {
        return pText.contains( mTemplateString ) || pText.contains( mTemplateStringLC );
    }

    public String apply( String pText ) {
        int zAt = pText.indexOf( mTemplateString );
        if ( zAt != -1 ) {
            return applyFrontReplacements( pText, zAt ) + mReplacementString + applyEndReplacements( pText, zAt + mTemplateString.length() );
        }
        if ( -1 != (zAt = pText.indexOf( mTemplateStringLC )) ) {
            return applyFrontReplacements( pText, zAt ) + mReplacementStringLC + applyEndReplacements( pText, zAt + mTemplateStringLC.length() );
        }
        return pText;
    }

    private String applyFrontReplacements( String pText, int pUpTo ) {
        return (pUpTo == 0) ? "" : apply( pText.substring( 0, pUpTo ) );
    }

    private String applyEndReplacements( String pText, int pFrom ) {
        return (pFrom == pText.length()) ? "" : apply( pText.substring( pFrom ) );
    }
}
