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
		case "A4-A1":
		case "S1":
			return 4001L; // Deux Montagnes - Brossard
		case "A":
		case "A0-A1":
		case "S2":
			return 1001L; // Bois Franc - Brossard // Gare Centrale - Brossard
		case "A3-A1":
		case "S3":
			return 3001L; // Anse-à-l'Orme - Brossard
		}
		return super.convertRouteIdFromShortNameNotSupported(routeShortName);
	}

	@Override
	public @NotNull String getRouteShortName(@NotNull GRoute gRoute) {
		if (!FeatureFlags.F_USE_GTFS_ID_HASH_INT) {
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

	@Override
	public boolean allowDuplicateKeyError() {
		return true; // 2025-08-13: duplicate keys in trips & calendar
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
			case "ST_A40":
				return 10_002;
			case "ST_BFC":
				return 10_005;
			case "ST_CAN":
				return 10_007;
			case "ST_DEM":
				return 10_009;
			case "ST_DSO":
				return 10_010;
			case "ST_DUQ":
				return 10_004;
			case "ST_EDM":
				return 10_011;
			case "ST_GCT": // ST_GCT_1
				return 10_012;
			case "ST_GRM":
				return 10_013;
			case "ST_IDS": // ST_IDS_1
				return 10_008;
			case "ST_ILB":
				return 10_014;
			case "ST_JYV":
				return 10_015;
			case "ST_MCG":
				return 10_016;
			case "ST_MPE":
				return 10_017;
			case "ST_MRL":
				return 10_018;
			case "ST_PAN":
				return 10_006;
			case "ST_PTC":
				return 10_019;
			case "ST_RIV": // ST_RIV_1
				return 10_001;
			case "ST_ROX":
				return 10_003;
			case "ST_RUI":
				return 10_020;
			case "ST_SAB":
				return 10_021;
			case "ST_SDR":
				return 10_022;
			case "ST_SUN":
				return 10_023;
			}
		}
		throw new MTLog.Fatal("Unexpected stop ID for %s!", gStop.toStringPlus(true));
	}

	@Override
	public @NotNull String getStopCode(@NotNull GStop gStop) {
		if (!FeatureFlags.F_USE_GTFS_ID_HASH_INT) {
			//noinspection DiscouragedApi
			return gStop.getStopId(); // use GTFS ID as stop code // used for GTFS-RT
		}
		return EMPTY; // no user facing stop code IRL
	}
}
