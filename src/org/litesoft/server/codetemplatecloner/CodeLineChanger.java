package org.litesoft.server.codetemplatecloner;

import org.litesoft.commonfoundation.annotations.*;

public interface CodeLineChanger {
    /**
     * This method is called to possibly change a "template" line by the CodeTemplateCloner before its normal processes.
     *
     * If the return value is unchanged, then the CodeTemplateCloner's normal processes are applied,
     * otherwise the return value (after de-nulling) is used as the substitution.
     */
    @NotNull String changeLine( @NotNull String pLine );
}
