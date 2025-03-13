package org.mtransit.parser.ca_montreal_rem_light_rail;

import static org.mtransit.commons.RegexUtils.BEGINNING;
import static org.mtransit.commons.RegexUtils.DIGIT_CAR;
import static org.mtransit.commons.RegexUtils.END;
import static org.mtransit.commons.RegexUtils.group;
import static org.mtransit.parser.Constants.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.Cleaner;
import org.mtransit.commons.FeatureFlags;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;

import java.util.List;
import java.util.Locale;

// https://rem.info/ (GTFS received by email)
public class MontrealREMLightRailAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new MontrealREMLightRailAgencyTools().start(args);
	}

	@Nullable
	@Override
	public List<Locale> getSupportedLanguages() {
		return LANG_FR;
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "REM";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_LIGHT_RAIL;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	@Override
	public @Nullable Long convertRouteIdFromShortNameNotSupported(@NotNull String routeShortName) {
		switch (routeShortName) {
		case "A":
		case "S2":
			return 1001L;
		}
		return super.convertRouteIdFromShortNameNotSupported(routeShortName);
	}

	@Override
	public @NotNull String getRouteShortName(@NotNull GRoute gRoute) {
		if (FeatureFlags.F_USE_GTFS_ID_HASH_INT) {
			//noinspection DiscouragedApi
			return gRoute.getRouteId(); // use GTFS ID as route short name // used for GTFS-RT
		}
		return super.getRouteShortName(gRoute);
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR = "72A300"; // light green // web site CSS

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.keepToFR(tripHeadsign);
		tripHeadsign = CleanUtils.cleanBounds(getFirstLanguageNN(), tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypesFRCA(tripHeadsign);
		return CleanUtils.cleanLabelFR(tripHeadsign);
	}

	private static final Cleaner STARTS_WITH_STATION_ = new Cleaner(group(BEGINNING + "station "), EMPTY, true);

	private static final Cleaner ENDS_WITH_Q_0_ = new Cleaner(group(" - quai " + DIGIT_CAR + END), EMPTY, true);

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = STARTS_WITH_STATION_.clean(gStopName);
		gStopName = ENDS_WITH_Q_0_.clean(gStopName);
		gStopName = CleanUtils.cleanBounds(getFirstLanguageNN(), gStopName);
		gStopName = CleanUtils.cleanStreetTypesFRCA(gStopName);
		return CleanUtils.cleanLabelFR(gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		//noinspection DiscouragedApi
		final String parentStationId = gStop.getParentStationId();
		if (parentStationId != null && parentStationId.length() >= 6) {
			String parentStationIdStartsWith = parentStationId.substring(0, 6);
			switch (parentStationIdStartsWith) {
			case "ST_DUQ":
				return 10_004;
			case "ST_GCT":
				return 10_012;
			case "ST_IDS":
				return 10_008;
			case "ST_PAN":
				return 10_006;
			case "ST_RIV":
				return 10_001;
			}
		}
		throw new MTLog.Fatal("Unexpected stop ID for %s!", gStop.toStringPlus(true));
	}

	@Override
	public @NotNull String getStopCode(@NotNull GStop gStop) {
		if (FeatureFlags.F_USE_GTFS_ID_HASH_INT) {
			//noinspection DiscouragedApi
			return gStop.getStopId(); // use GTFS ID as stop code // used for GTFS-RT
		}
		return EMPTY; // no user facing stop code IRL
	}
}
