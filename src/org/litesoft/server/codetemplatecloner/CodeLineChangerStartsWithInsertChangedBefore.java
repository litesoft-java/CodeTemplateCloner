package org.litesoft.server.codetemplatecloner;

import org.litesoft.commonfoundation.annotations.*;
import org.litesoft.commonfoundation.base.*;

import java.util.*;

public class CodeLineChangerStartsWithInsertChangedBefore implements CodeLineChanger {
    private final Replacer mReplacer;
    private final String mStartsWith;

    public CodeLineChangerStartsWithInsertChangedBefore( Replacer pReplacer, String pStartsWith ) {
        mReplacer = Confirm.isNotNull( "Replacer", pReplacer );
        mStartsWith = Confirm.isNotNullOrEmpty( "StartsWith", pStartsWith );
    }

    /**
     * This method is called to possibly change a "template" line by the CodeTemplateCloner before its normal processes.
     * <p/>
     * If the return value is unchanged, then the CodeTemplateCloner's normal processes are applied,
     * otherwise the return value if NOT null is used as the substitution.
     */
    public @NotNull String changeLine( List<String> pCollector, @NotNull String pLine ) {
        if ( pLine.startsWith( mStartsWith ) ) {
            pCollector.add( mReplacer.apply( pLine ) );
            pCollector.add( pLine );
            return null;
        }
        return pLine;
    }
}
