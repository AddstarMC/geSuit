/*
 * The MIT License (MIT)
 * Copyright (c) 2015 - 2020., AddstarMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal  in the Software without restriction, including without limitation the
 * rights  to use, copy, modify, merge, publish, distribute, sublicense,and/or
 * sell  copies of the Software, and to permit persons to whom the Software
 * is  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.cubespace.geSuit.managers;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.DatabaseReader.Builder;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.AsnResponse;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Traits;
import net.cubespace.geSuit.geSuit;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility Class for GeoIP.
 */
public class GeoIPManager {
    /**
     * If true output will show city.
     */
    private static boolean showCity = true;
    /**
     * Show Company name.
     */
    private static boolean showASN = false;
    /**
     * Show Extra Details.
     */
    private static boolean showDetail = false;
    /**
     * The Reader for country info.
     */
    private static DatabaseReader dbCountryReader;
    /**
     * The Reader for city info.
     */
    private static DatabaseReader dbCityReader;
    /**
     * The Reader for corporate info.
     */
    private static DatabaseReader dbASNReader;

    /**
     * Used to initialize the class.
     */
    public static void initialize() {
        File cityFile = new File(
              ConfigManager.bans.GeoIP.pathToGeoIPFiles
                    + ConfigManager.bans.GeoIP.geoIPCity);
        File countryFile = new File(
              ConfigManager.bans.GeoIP.pathToGeoIPFiles
                    + ConfigManager.bans.GeoIP.geoIPCountry);
        File asnFile = new File(
              ConfigManager.bans.GeoIP.pathToGeoIPFiles
                    + ConfigManager.bans.GeoIP.geoIPASN);
        showCity = ConfigManager.bans.GeoIP.ShowCity != null ? ConfigManager.bans.GeoIP.ShowCity : true;
        showDetail = ConfigManager.bans.GeoIP.ShowDetail != null ? ConfigManager.bans.GeoIP.ShowDetail : false;
        showASN = ConfigManager.bans.GeoIP.ShowAsn != null ? ConfigManager.bans.GeoIP.ShowAsn : false;
        if (!countryFile.exists()) {
            geSuit.getInstance().getLogger().warn("[GeoIP] No GeoIP"
                  + " database is available locally.  Please install and "
                  + "run GeoIpupdate from MaxMind");
            return;
        }
        try {
            dbCountryReader = new Builder(countryFile).build();
        } catch (IOException e) {
            geSuit.getInstance().getLogger().warn("[GeoIP] Unable to "
                    + "read GeoIP database, if this No GeoIP database is "
                  + " available locally and updating is off. Lookups"
                  + " will be unavailable");
        }
        try {
            dbCityReader = new DatabaseReader.Builder(cityFile).build();
        } catch (IOException e) {
            geSuit.getInstance().getLogger().warn("[GeoIP] Unable to "
                  + "read GeoIP City database, City records unavailable");
            showCity = false;
        }
        if (showASN) {
            try {
                dbASNReader = new DatabaseReader.Builder(asnFile).build();
            } catch (IOException e) {
                geSuit.getInstance().getLogger().warn("[GeoIP] Unable to"
                      + " read GeoIP ASN database, ASN records unavailable");
                showASN = false;
            }
        }
    }

    /**
     * Simple 1 line output lookup.
     * @param address {@code InetAddress}
     * @return String
     */
    public static String lookup(final InetAddress address) {
        return detailLookup(address).get(0);
    }

    /**
     * Generate a detailed Lookup.
     * @param address {@code InetAddress}
     * @return a list of messages
     */
    @SuppressWarnings("WeakerAccess")
    public static List<String> detailLookup(final InetAddress address) {
        List<String> response = new ArrayList<>();
        try {
            if (address == InetAddress.getLoopbackAddress()
                  || address.isAnyLocalAddress()) {
                return Collections.singletonList("localhost");
            }
            if (dbCountryReader == null) {
                return Collections.singletonList("NA");
            }
            if (!showCity && !showASN) {
                return getCountry(address);
            }
            if (showCity) {
                response = getCity(address);
            }
            if (showASN) {
                try {
                    String out;
                    if (!response.isEmpty()) {
                        out = response.get(0);
                    } else {
                        out = "";
                    }
                    AsnResponse asnResponse = dbASNReader.asn(address);
                    String organization =
                          asnResponse.getAutonomousSystemOrganization();
                    response.remove(0);
                    response.add(0, organization + ", " + out);
                } catch (IOException | GeoIp2Exception e) {
                    geSuit.getInstance().getLogger().warn("[GeoIP] Unable"
                          + " to read GeoIP ASN database, ASN records"
                          + " unavailable");
                    showASN = false;
                    geSuit.getInstance().getLogger().warn("[GeoIP] "
                          + e.getLocalizedMessage());
                }
            }
            return response;
        } catch (Throwable err) {
            geSuit.getInstance().getLogger().warn(err.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Add extra details.
     * @param response List
     * @param traits Traits
     * @return List of Strings.
     */
    private static List<String> addDetails(final List<String> response,
                                           final Traits traits) {
        if (showDetail) {
            response.add("Tor Node: " + traits.isTorExitNode());
            response.add("Public Proxy: " + traits.isPublicProxy());
            response.add("VPN Node: " + traits.isAnonymousVpn());
            response.add("Connection Type: "
                    + traits.getConnectionType().toString());
            response.add("Domain: " + traits.getDomain().toLowerCase());
            response.add("ISP: " + traits.getIsp());
            response.add("Static IP Score: " + traits.getStaticIpScore());
        }
        return response;
    }

    /**
     * Add City details.
     * @param address {@code InetAddress}
     * @return List
     */
    private static List<String> getCity(final InetAddress address) {
        List<String> response = new ArrayList<>();
        try {
            StringBuilder out = new StringBuilder();
            CityResponse cityResponse = dbCityReader.city(address);
            String city = cityResponse.getCity().getName();
            out.append(city);
            out.append(", ");
            String region = cityResponse.getMostSpecificSubdivision().getName();
            out.append(region);
            out.append(", ");
            String country = cityResponse.getCountry().getName();
            out.append(country);
            response.add(out.toString());
            return addDetails(response, cityResponse.getTraits());
        } catch (IOException | GeoIp2Exception e) {
            geSuit.getInstance().getLogger().warn("[GeoIP] Unable"
                    + " to read GeoIP City database, city records unavailable");
            geSuit.getInstance().getLogger().warn("[GeoIP] "
                    + e.getLocalizedMessage());
            return getCountry(address);
        }

    }

    /**
     * Add the country.
     * @param address {@code InetAddress}
     * @return List
     */
    private static List<String> getCountry(final InetAddress address) {
        List<String> response = new ArrayList<>();
        try {
            CountryResponse countryResponse = dbCountryReader.country(address);
            response.add(countryResponse.getCountry().getName());
            return addDetails(response, countryResponse.getTraits());
        } catch (GeoIp2Exception | IOException e) {
            geSuit.getInstance().getLogger().warn("[GeoIP] Unable"
                    + " to read GeoIP Country database, Country "
                    + "records unavailable");
            geSuit.getInstance().getLogger().warn("[GeoIP] "
                    + e.getLocalizedMessage());
            response.add("NA");
        }
        return response;
    }
}
