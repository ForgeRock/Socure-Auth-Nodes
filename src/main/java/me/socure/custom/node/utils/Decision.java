package me.socure.custom.node.utils;////////////////////////////////////////////////////////////////////////////////

/**
 * Outcomes Ids for this node.
 */
public enum Decision {
    Reject, Refer, Resubmit, Review, Accept, Error;

    /**
     * from
     * @param strDecision strDecision
     * @return Decision Decision
     */
    public static Decision from(String strDecision) {
        for (Decision token : values()) {
            if (token.name().equalsIgnoreCase(strDecision)) return token;
        }
        return Error;
    }
}