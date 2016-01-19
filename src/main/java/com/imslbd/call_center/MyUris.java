package com.imslbd.call_center;

/**
 * Created by someone on 12/11/2015.
 */
public enum MyUris {
    STATIC_RESOURCES_PATTERN("/my-static/*", "Static Resources"),
    PUBLIC_RESOURCES_PATTERN("/my-public/*", "Public Resources"),

    DASHBOARD("/dashboard", "Dashboard"),
    GOOGLE_MAP("/google-map", "Google Map"),
    SEARCH_DISTRIBUTION_HOUSES("/search-territories", "Search Territory"),
    SEARCH_REGIONS("/search-regions", "Search Regions"),
    STEP_1("/step1", "Step1"),
    STEP_2("/step2", "step2"),
    AREAS("/areas", "Areas"),
    DISTRIBUTION_HOUSES("/distribution-houses", "Distribution Houses"),
    BRS("/brs", "BRS"),
    CONSUMER_CONTACTS_CALL_STEP_1("/consumer-contacts/call-step-1", "Consumer Contact Call Step 1"),
    CONSUMER_CONTACTS_CALL_STEP_2("/consumer-contacts/call-step-2", "Consumer Contact Call Step 2"),
    CONTACT_DETAILS("/consumer-contacts/details", ""),
    CALL_OPERATOR("/call-operator", ""),
    CURRENT_USER("/current-user", ""),
    CALL_CREATE("/call/create", ""),
    BRANDS("/brands", ""),
    BR_ACTIVITY_SUMMARY("/br-activity-summary", "Br Activity Summary");

    public final String value;
    public final String label;

    MyUris(final String value, String label) {
        this.value = value;
        this.label = label;
    }
}
