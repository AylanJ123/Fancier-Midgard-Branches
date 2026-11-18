package com.aylanj123.fanciermidgardbranches.client;

final class ConnectionRules {
    static final int UP = 16;
    static final int DOWN = 32;

    static int chooseMask(int horizontal, boolean leavesAbove, boolean branchAbove,
                          boolean woodAbove, boolean connectedBelow) {
        return chooseMask(horizontal, leavesAbove, branchAbove, woodAbove, connectedBelow, horizontal != 0);
    }

    static int chooseMask(int horizontal, boolean leavesAbove, boolean branchAbove,
                          boolean woodAbove, boolean connectedBelow, boolean horizontalBranch) {
        int result = horizontal & 15;
        if (connectedBelow) {
            result |= DOWN;
        }
        if (leavesAbove || !horizontalBranch && (woodAbove || branchAbove)) {
            result |= UP;
        }
        return result == 0 ? UP | DOWN : result;
    }
}
