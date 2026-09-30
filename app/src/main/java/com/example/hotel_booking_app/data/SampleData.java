package com.example.hotel_booking_app.data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class SampleData {

    public static final GuestProfile PROFILE =
            new GuestProfile("Aditi Sharma", "aditi.sharma@example.com", "+91 98250 11223");

    private static final class HotelSeed {
        final Hotel hotel;
        final int baseRate;
        final int units;

        HotelSeed(Hotel hotel, int baseRate, int units) {
            this.hotel = hotel;
            this.baseRate = baseRate;
            this.units = units;
        }
    }

    private static final class RoomDetail {
        final String beds;
        final int sizeSqm;
        final int unitAdjustment;
        final int multiplier;

        RoomDetail(String beds, int sizeSqm, int unitAdjustment, int multiplier) {
            this.beds = beds;
            this.sizeSqm = sizeSqm;
            this.unitAdjustment = unitAdjustment;
            this.multiplier = multiplier;
        }
    }

    private SampleData() {
    }

    private static final List<HotelSeed> SEEDS = Arrays.asList(
            new HotelSeed(new Hotel("htl-mumbai", "Marine Drive Grand", "Mumbai", "India",
                    "142 Netaji Subhash Chandra Marg",
                    "A landmark seafront hotel facing the Arabian Sea, with a rooftop pool, all-day "
                            + "dining and rooms framed by the curve of Queen's Necklace.",
                    5, 4.7f, 1284,
                    Arrays.asList(Amenity.WIFI, Amenity.POOL, Amenity.SPA, Amenity.BREAKFAST, Amenity.GYM),
                    Arrays.asList("Sea-facing rooms", "Rooftop pool bar", "10 min to the Gateway of India"),
                    0), 12000, 4),
            new HotelSeed(new Hotel("htl-goa", "Palm Cove Resort", "Goa", "India",
                    "Candolim Beach Road",
                    "Barefoot luxury a short walk from Candolim beach: garden villas, a lagoon pool "
                            + "and a beachside kitchen serving the morning's catch.",
                    4, 4.5f, 862,
                    Arrays.asList(Amenity.WIFI, Amenity.POOL, Amenity.BREAKFAST, Amenity.PARKING, Amenity.PET_FRIENDLY),
                    Arrays.asList("2 min walk to the beach", "Lagoon pool", "Free bike hire"),
                    1), 9000, 5),
            new HotelSeed(new Hotel("htl-jaipur", "Amber Heritage Haveli", "Jaipur", "India",
                    "12 Amer Road, Old City",
                    "A restored 19th-century haveli with painted courtyards, a candlelit rooftop "
                            + "restaurant and views of the Amer Fort walls.",
                    4, 4.6f, 543,
                    Arrays.asList(Amenity.WIFI, Amenity.BREAKFAST, Amenity.SPA, Amenity.AIRPORT_SHUTTLE),
                    Arrays.asList("Rooftop dinners", "Walk to the fort", "Cooking classes on request"),
                    2), 7500, 3),
            new HotelSeed(new Hotel("htl-delhi", "Lutyens Bungalow Hotel", "Delhi", "India",
                    "4 Prithviraj Road",
                    "A colonial-era bungalow turned heritage hotel in Lutyens' Delhi, with a manicured "
                            + "lawn, a fine-dining courtyard and Humayun's Tomb minutes away.",
                    5, 4.6f, 1745,
                    Arrays.asList(Amenity.WIFI, Amenity.GYM, Amenity.SPA, Amenity.BREAKFAST, Amenity.AIRPORT_SHUTTLE),
                    Arrays.asList("Lawn dining", "10 min to Humayun's Tomb", "Airport transfers on request"),
                    3), 11000, 4),
            new HotelSeed(new Hotel("htl-udaipur", "Lake Pichola Palace", "Udaipur", "India",
                    "Lake Palace Road, Pichola",
                    "A lakefront haveli with domed pavilions and a rooftop restaurant looking straight "
                            + "across Lake Pichola to the City Palace.",
                    5, 4.8f, 1032,
                    Arrays.asList(Amenity.WIFI, Amenity.POOL, Amenity.SPA, Amenity.BREAKFAST, Amenity.PARKING),
                    Arrays.asList("Lake-facing rooftop dining", "Boat rides at sunset", "Views of the City Palace"),
                    4), 14000, 3),
            new HotelSeed(new Hotel("htl-agra", "Taj View Heritage", "Agra", "India",
                    "22 Taj East Gate Road",
                    "A heritage property a short walk from the Taj Mahal's east gate, with a terrace "
                            + "restaurant that watches the marble change colour at dawn.",
                    4, 4.5f, 987,
                    Arrays.asList(Amenity.WIFI, Amenity.BREAKFAST, Amenity.PARKING, Amenity.AIRPORT_SHUTTLE),
                    Arrays.asList("Sunrise Taj Mahal views", "5 min to the east gate", "Guided city tours"),
                    5), 8000, 5),
            new HotelSeed(new Hotel("htl-munnar", "Misty Tea Estate Retreat", "Munnar", "India",
                    "Pallivasal Estate Road",
                    "Cottages set inside a working tea estate in the Western Ghats, with mist rolling "
                            + "over the plantation each morning and a spa built for the chill.",
                    4, 4.7f, 654,
                    Arrays.asList(Amenity.WIFI, Amenity.SPA, Amenity.BREAKFAST, Amenity.PARKING),
                    Arrays.asList("Tea estate walks", "Mountain-view cottages", "Bonfire evenings"),
                    6), 7000, 3),
            new HotelSeed(new Hotel("htl-manali", "Solang Valley Chalet", "Manali", "India",
                    "Solang Valley Road",
                    "Wood-and-stone chalets facing the Solang slopes, with a bonfire deck, "
                            + "mountain-view rooms and easy access to the ropeway.",
                    4, 4.5f, 812,
                    Arrays.asList(Amenity.WIFI, Amenity.BREAKFAST, Amenity.PARKING, Amenity.PET_FRIENDLY),
                    Arrays.asList("Solang ropeway nearby", "Bonfire deck", "Mountain-facing rooms"),
                    7), 6500, 4),
            new HotelSeed(new Hotel("htl-rishikesh", "Ganga Riverside Retreat", "Rishikesh", "India",
                    "Tapovan, Laxman Jhula Road",
                    "A riverside retreat above the Ganga with a yoga deck, a cafe overlooking Laxman "
                            + "Jhula and the evening Ganga Aarti a short walk away.",
                    4, 4.6f, 728,
                    Arrays.asList(Amenity.WIFI, Amenity.BREAKFAST, Amenity.PARKING),
                    Arrays.asList("Riverside yoga deck", "Walk to the Ganga Aarti", "Cafe with river views"),
                    8), 6000, 5),
            new HotelSeed(new Hotel("htl-varanasi", "Ghat View Heritage Hotel", "Varanasi", "India",
                    "Assi Ghat Road",
                    "A restored haveli overlooking Assi Ghat, with rooftop views of the sunrise boat "
                            + "traffic and the evening aarti on the Ganga.",
                    4, 4.5f, 693,
                    Arrays.asList(Amenity.WIFI, Amenity.BREAKFAST, Amenity.AIRPORT_SHUTTLE),
                    Arrays.asList("Rooftop ghat views", "Sunrise boat rides", "Walk to the evening aarti"),
                    9), 6800, 4),
            new HotelSeed(new Hotel("htl-amritsar", "Golden Temple View Hotel", "Amritsar", "India",
                    "12 Golden Temple Road",
                    "A five-minute walk from the Golden Temple, with rooftop dining over the old city "
                            + "and complimentary transfers for the langar hall.",
                    4, 4.6f, 1204,
                    Arrays.asList(Amenity.WIFI, Amenity.BREAKFAST, Amenity.PARKING, Amenity.AIRPORT_SHUTTLE),
                    Arrays.asList("5 min to the Golden Temple", "Rooftop dining", "Old city views"),
                    10), 6200, 5),
            new HotelSeed(new Hotel("htl-pondicherry", "French Quarter Boutique Hotel", "Pondicherry", "India",
                    "14 Rue Romain Rolland",
                    "A pastel colonial townhouse in the French Quarter, with a courtyard cafe, "
                            + "bicycles for the promenade and the beach a short ride away.",
                    4, 4.6f, 589,
                    Arrays.asList(Amenity.WIFI, Amenity.BREAKFAST, Amenity.PET_FRIENDLY),
                    Arrays.asList("French Quarter courtyard", "Free bicycle hire", "10 min to the beach"),
                    11), 7200, 3)
    );

    private static RoomDetail detailFor(RoomType type) {
        switch (type) {
            case STANDARD:
                return new RoomDetail("1 queen bed", 24, 0, 100);
            case DELUXE:
                return new RoomDetail("1 king bed", 34, 0, 150);
            case SUITE:
            default:
                return new RoomDetail("1 king bed + sofa bed", 56, -1, 240);
        }
    }

    public static List<Hotel> hotels() {
        List<Hotel> hotels = new ArrayList<>();
        for (HotelSeed seed : SEEDS) {
            hotels.add(seed.hotel);
        }
        return hotels;
    }

    public static List<RoomOffer> offers() {
        List<RoomOffer> offers = new ArrayList<>();
        for (HotelSeed seed : SEEDS) {
            for (RoomType type : RoomType.values()) {
                RoomDetail detail = detailFor(type);
                int nightlyRate = seed.baseRate * detail.multiplier / 100 / 100 * 100;
                int units = Math.max(seed.units + detail.unitAdjustment, 5);
                offers.add(new RoomOffer(
                        seed.hotel.id + "-" + type.name().toLowerCase(Locale.ROOT),
                        seed.hotel.id,
                        type,
                        nightlyRate,
                        units,
                        detail.beds,
                        detail.sizeSqm));
            }
        }
        return offers;
    }

    public static List<Review> reviews(LocalDate today) {
        return Arrays.asList(
                review("rv-1", "htl-mumbai", "Marcus Bell", 5f, "Woke up to the whole bay from bed. Staff remembered our names by day two.", today.minusDays(18)),
                review("rv-2", "htl-mumbai", "Lena Fischer", 4.5f, "Superb breakfast and the rooftop pool at sunset is unbeatable. Lifts can be slow.", today.minusDays(44)),
                review("rv-3", "htl-goa", "Sofia Rossi", 4.5f, "Quiet, green and a two-minute walk to the sand. Perfect for a slow week.", today.minusDays(9)),
                review("rv-4", "htl-goa", "James Okoro", 4f, "Great pool and friendly service. Wi-Fi was patchy near the villas.", today.minusDays(31)),
                review("rv-5", "htl-jaipur", "Rahul Menon", 5f, "Like sleeping inside a museum, with far better beds. The rooftop dinner was the highlight.", today.minusDays(22)),
                review("rv-6", "htl-jaipur", "Priya Nair", 4.5f, "Beautifully restored and easy to reach the fort. Book the courtyard rooms.", today.minusDays(57)),
                review("rv-7", "htl-delhi", "Marcus Bell", 5f, "Immaculate service and the lawn dinner was a lovely touch.", today.minusDays(12)),
                review("rv-8", "htl-delhi", "Aditi Sharma", 4.5f, "Quiet, heritage feel and close to Humayun's Tomb.", today.minusDays(63)),
                review("rv-9", "htl-udaipur", "Lena Fischer", 4.5f, "The lake view at dusk is worth the trip alone. Steep steps, so pack light.", today.minusDays(15)),
                review("rv-10", "htl-udaipur", "Sofia Rossi", 4f, "Charming and central. Breakfast on the rooftop was the best of our trip.", today.minusDays(40)),
                review("rv-11", "htl-munnar", "Rahul Menon", 5f, "Our cottage looked over the tea estate and the staff arranged everything, from walks to a driver.", today.minusDays(7)),
                review("rv-12", "htl-munnar", "James Okoro", 5f, "The most peaceful place I have stayed. The mist over the plantation is unreal.", today.minusDays(29)),
                review("rv-13", "htl-manali", "Marcus Bell", 4.5f, "The mountain view is unreal and the bonfire deck is properly cosy. Pricey taxis.", today.minusDays(11)),
                review("rv-14", "htl-manali", "Priya Nair", 4.5f, "Comfortable rooms and excellent breakfast. Easy access to the ropeway.", today.minusDays(36)),
                review("rv-15", "htl-rishikesh", "Lena Fischer", 5f, "Riverside yoga at sunrise and staff who booked our rafting trip.", today.minusDays(20)),
                review("rv-16", "htl-rishikesh", "Sofia Rossi", 4f, "Lovely, but the lane is steep and rooms are simple.", today.minusDays(52)),
                review("rv-17", "htl-varanasi", "Rahul Menon", 5f, "Spotless, calm and the ghat view at sunrise is a treat.", today.minusDays(8)),
                review("rv-18", "htl-varanasi", "Aditi Sharma", 4.5f, "Ideal base for the ghats. Walking distance to the evening aarti made it easy.", today.minusDays(47)),
                review("rv-19", "htl-amritsar", "James Okoro", 4f, "Great to be so close to the Golden Temple. Some street noise on lower floors.", today.minusDays(14)),
                review("rv-20", "htl-amritsar", "Marcus Bell", 4.5f, "Rooftop dining is huge by local standards and the staff are properly attentive.", today.minusDays(38)),
                review("rv-21", "htl-pondicherry", "Priya Nair", 5f, "The bicycle hire around the French Quarter is a lovely touch. Best coffee of my trip.", today.minusDays(10)),
                review("rv-22", "htl-pondicherry", "Sofia Rossi", 4.5f, "Gorgeous courtyard and wonderful staff. Book the sea-facing rooms.", today.minusDays(33)),
                review("rv-23", "htl-agra", "Lena Fischer", 5f, "Worth every rupee. Watching the sunrise over the Taj from our own terrace is something I will remember.", today.minusDays(6)),
                review("rv-24", "htl-agra", "Aditi Sharma", 5f, "Immaculate room, kind hosts and a breakfast delivered with a Taj view.", today.minusDays(25))
        );
    }

    public static List<Booking> bookings(LocalDate today) {
        return Arrays.asList(
                booking("bk-1001", "htl-goa-deluxe", 13500, today.plusDays(12), today.plusDays(16), 2, BookingStatus.CONFIRMED, "Sea-facing room if possible", today.minusDays(6), true, 10, false),
                booking("bk-1002", "htl-delhi-standard", 11000, today.plusDays(30), today.plusDays(33), 1, BookingStatus.CONFIRMED, "", today.minusDays(2), false, 0, true),
                booking("bk-1003", "htl-jaipur-suite", 18000, today.minusDays(40), today.minusDays(37), 3, BookingStatus.CONFIRMED, "Rooftop dinner on the first night", today.minusDays(70), true, 0, false),
                booking("bk-1004", "htl-mumbai-standard", 12000, today.minusDays(90), today.minusDays(88), 2, BookingStatus.CONFIRMED, "", today.minusDays(100), false, 0, false),
                booking("bk-1005", "htl-udaipur-deluxe", 21000, today.plusDays(20), today.plusDays(22), 2, BookingStatus.CANCELLED, "", today.minusDays(14), false, 0, false)
        );
    }

    private static Review review(String id, String hotelId, String guestName, float rating,
                                 String comment, LocalDate date) {
        return new Review(id, hotelId, "", guestName, rating, comment, date);
    }

    private static Booking booking(String id, String roomId, int nightlyRate, LocalDate checkIn,
                                   LocalDate checkOut, int guests, BookingStatus status,
                                   String specialRequests, LocalDate bookedOn,
                                   boolean breakfastIncluded, int promoPercent, boolean payAtHotel) {
        String hotelId = roomId.substring(0, roomId.lastIndexOf('-'));
        return new Booking(id, hotelId, roomId, PROFILE.name, PROFILE.phone, PROFILE.email,
                checkIn, checkOut, guests, status, specialRequests, bookedOn, nightlyRate,
                breakfastIncluded, promoPercent, payAtHotel);
    }
}
