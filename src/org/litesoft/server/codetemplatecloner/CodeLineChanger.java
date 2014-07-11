package org.litesoft.server.codetemplatecloner;

import org.litesoft.commonfoundation.annotations.*;

public interface CodeLineChanger {
    /**
     * This method is called to possibly change a "template" line by the CodeTemplateCloner before its normal processes.
     * <p/>
     * If the return value is unchanged, then the CodeTemplateCloner's normal processes are applied,
     * otherwise the return value (after de-nulling) is used as the substitution.
     */
    @NotNull String changeLine( @NotNull String pLine );

    public static final CodeLineChanger NO_OP = new CodeLineChanger() {
        @Override
        public String changeLine( @NotNull String pLine ) {
            return pLine;
        }
    };
}
