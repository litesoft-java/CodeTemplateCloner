package org.litesoft.server.codetemplatecloner;

import org.litesoft.commonfoundation.annotations.*;
import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.typeutils.*;

import java.util.*;

public class CodeLineChangerSet implements CodeLineChanger {
    private final List<CodeLineChanger> mChangers = Lists.newArrayList();

    public CodeLineChangerSet( CodeLineChanger... pChangers ) {
        for ( CodeLineChanger zChanger : ConstrainTo.notNullImmutableList( pChangers ) ) {
            if ( zChanger != null ) {
                mChangers.add( zChanger );
            }
        }
    }

    /**
     * This method is called to possibly change a "template" line by the CodeTemplateCloner before its normal processes.
     * <p/>
     * If the return value is unchanged, then the CodeTemplateCloner's normal processes are applied,
     * otherwise the return value if NOT null is used as the substitution.
     */
    public
    @NotNull
    String changeLine( List<String> pCollector, @NotNull String pLine ) {
        for ( CodeLineChanger zChanger : mChangers ) {
            String zChangedLine = zChanger.changeLine( pCollector, pLine );
            if ( !pLine.equals( zChangedLine ) ) {
                return zChangedLine;
            }
        }
        return pLine;
    }
}
