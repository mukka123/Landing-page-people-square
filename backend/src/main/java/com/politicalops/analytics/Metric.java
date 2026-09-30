package com.politicalops.analytics;

/** Matches the "Key analytical measures" table in the HLD. */
public enum Metric {
    TURNOUT_PCT,       // votes polled / electors, per the elections configured definition
    VOTE_SHARE,        // candidate votes / valid votes at the selected geography
    LEAD,              // first-placed votes minus second-placed votes
    SWING,             // change in party vote share between comparable geography snapshots
    CONCENTRATION,     // share of a party total originating from selected clusters
    OPERATIONAL_COVERAGE, // completed field units / planned field units (no voter-pref labels)
    ISSUE_AGING,       // now - creation time, grouped by SLA and status
    TASK_COMPLETION    // weighted completed / planned tasks for a period+scope
}
