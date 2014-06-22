package com.swentel.druglass;

/**
 * Configuration file.
 */
public class Config {

    // Note that currently the drupapp module does not return any feedback
    // so far, so make sure your settings here and on the Drupal site are correct.

    // Glass secret - set this to the secret as in your Drupal configuration.
    public static String glassSecret = "somethingSomething";

    // Drupal Glass endpoint.
    public static String glassEndPoint = "http://example.com/endpoint";

    // Drupal user account - take an account which does not have to many privileges.
    public static String drupalAccountName = "iCanUploadImages";

    // Title of the image node.
    public static String imageTitle = "Send from Glass";
}
