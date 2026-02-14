package net.cubespace.geSuit.configs.SubConfig;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.YamlConfig;

public class GeoIPSettings extends YamlConfig {

    @Comment("GeoIP On / Off")
    public Boolean ShowOnLogin = true;
    @Comment("Customise the path to your GeoIP2 Files")
    public String pathToGeoIPFiles = "/usr/share/GeoIP/";

    @Comment("GeoIP City Database")
    public String geoIPCity = "GeoLite2-City.mmdb";

    @Comment("GeoIP City Database")
    public String geoIPCountry = "GeoLite2-Country.mmdb";

    @Comment("GeoIP City Database")
    public String geoIPASN = "GeoLite2-ASN.mmdb";

    @Comment("Show GeoIP City")
    public Boolean ShowCity = true;

    @Comment("Show GeoIP Trait details")
    public Boolean ShowDetail = false;

    @Comment("Show GeoIP Organization")
    public Boolean ShowAsn = false;
}
