package com.example.hotel_booking_app.data

import java.time.LocalDate

object SampleData {

    val profile = GuestProfile(
        name = "Aditi Sharma",
        email = "aditi.sharma@example.com",
        phone = "+91 98250 11223"
    )

    private data class HotelSeed(val hotel: Hotel, val baseRate: Int, val units: Int)

    private val seeds = listOf(
        HotelSeed(
            Hotel(
                id = "htl-mumbai",
                name = "Marine Drive Grand",
                city = "Mumbai",
                country = "India",
                address = "142 Netaji Subhash Chandra Marg",
                description = "A landmark seafront hotel facing the Arabian Sea, with a rooftop " +
                    "pool, all-day dining and rooms framed by the curve of Queen's Necklace.",
                stars = 5,
                rating = 4.7f,
                reviewCount = 1284,
                amenities = listOf(Amenity.WIFI, Amenity.POOL, Amenity.SPA, Amenity.BREAKFAST, Amenity.GYM),
                highlights = listOf("Sea-facing rooms", "Rooftop pool bar", "10 min to the Gateway of India"),
                photoIndex = 0
            ),
            baseRate = 12000,
            units = 4
        ),
        HotelSeed(
            Hotel(
                id = "htl-goa",
                name = "Palm Cove Resort",
                city = "Goa",
                country = "India",
                address = "Candolim Beach Road",
                description = "Barefoot luxury a short walk from Candolim beach: garden villas, " +
                    "a lagoon pool and a beachside kitchen serving the morning's catch.",
                stars = 4,
                rating = 4.5f,
                reviewCount = 862,
                amenities = listOf(Amenity.WIFI, Amenity.POOL, Amenity.BREAKFAST, Amenity.PARKING, Amenity.PET_FRIENDLY),
                highlights = listOf("2 min walk to the beach", "Lagoon pool", "Free bike hire"),
                photoIndex = 1
            ),
            baseRate = 9000,
            units = 5
        ),
        HotelSeed(
            Hotel(
                id = "htl-jaipur",
                name = "Amber Heritage Haveli",
                city = "Jaipur",
                country = "India",
                address = "12 Amer Road, Old City",
                description = "A restored 19th-century haveli with painted courtyards, " +
                    "a candlelit rooftop restaurant and views of the Amer Fort walls.",
                stars = 4,
                rating = 4.6f,
                reviewCount = 543,
                amenities = listOf(Amenity.WIFI, Amenity.BREAKFAST, Amenity.SPA, Amenity.AIRPORT_SHUTTLE),
                highlights = listOf("Rooftop dinners", "Walk to the fort", "Cooking classes on request"),
                photoIndex = 2
            ),
            baseRate = 7500,
            units = 3
        ),
        HotelSeed(
            Hotel(
                id = "htl-delhi",
                name = "Lutyens Bungalow Hotel",
                city = "Delhi",
                country = "India",
                address = "4 Prithviraj Road",
                description = "A colonial-era bungalow turned heritage hotel in Lutyens' Delhi, " +
                    "with a manicured lawn, a fine-dining courtyard and Humayun's Tomb minutes away.",
                stars = 5,
                rating = 4.6f,
                reviewCount = 1745,
                amenities = listOf(Amenity.WIFI, Amenity.GYM, Amenity.SPA, Amenity.BREAKFAST, Amenity.AIRPORT_SHUTTLE),
                highlights = listOf("Lawn dining", "10 min to Humayun's Tomb", "Airport transfers on request"),
                photoIndex = 3
            ),
            baseRate = 11000,
            units = 4
        ),
        HotelSeed(
            Hotel(
                id = "htl-udaipur",
                name = "Lake Pichola Palace",
                city = "Udaipur",
                country = "India",
                address = "Lake Palace Road, Pichola",
                description = "A lakefront haveli with domed pavilions and a rooftop restaurant " +
                    "looking straight across Lake Pichola to the City Palace.",
                stars = 5,
                rating = 4.8f,
                reviewCount = 1032,
                amenities = listOf(Amenity.WIFI, Amenity.POOL, Amenity.SPA, Amenity.BREAKFAST, Amenity.PARKING),
                highlights = listOf("Lake-facing rooftop dining", "Boat rides at sunset", "Views of the City Palace"),
                photoIndex = 4
            ),
            baseRate = 14000,
            units = 3
        ),
        HotelSeed(
            Hotel(
                id = "htl-agra",
                name = "Taj View Heritage",
                city = "Agra",
                country = "India",
                address = "22 Taj East Gate Road",
                description = "A heritage property a short walk from the Taj Mahal's east gate, " +
                    "with a terrace restaurant that watches the marble change colour at dawn.",
                stars = 4,
                rating = 4.5f,
                reviewCount = 987,
                amenities = listOf(Amenity.WIFI, Amenity.BREAKFAST, Amenity.PARKING, Amenity.AIRPORT_SHUTTLE),
                highlights = listOf("Sunrise Taj Mahal views", "5 min to the east gate", "Guided city tours"),
                photoIndex = 5
            ),
            baseRate = 8000,
            units = 5
        ),
        HotelSeed(
            Hotel(
                id = "htl-munnar",
                name = "Misty Tea Estate Retreat",
                city = "Munnar",
                country = "India",
                address = "Pallivasal Estate Road",
                description = "Cottages set inside a working tea estate in the Western Ghats, " +
                    "with mist rolling over the plantation each morning and a spa built for the chill.",
                stars = 4,
                rating = 4.7f,
                reviewCount = 654,
                amenities = listOf(Amenity.WIFI, Amenity.SPA, Amenity.BREAKFAST, Amenity.PARKING),
                highlights = listOf("Tea estate walks", "Mountain-view cottages", "Bonfire evenings"),
                photoIndex = 6
            ),
            baseRate = 7000,
            units = 3
        ),
        HotelSeed(
            Hotel(
                id = "htl-manali",
                name = "Solang Valley Chalet",
                city = "Manali",
                country = "India",
                address = "Solang Valley Road",
                description = "Wood-and-stone chalets facing the Solang slopes, with a bonfire " +
                    "deck, mountain-view rooms and easy access to the ropeway.",
                stars = 4,
                rating = 4.5f,
                reviewCount = 812,
                amenities = listOf(Amenity.WIFI, Amenity.BREAKFAST, Amenity.PARKING, Amenity.PET_FRIENDLY),
                highlights = listOf("Solang ropeway nearby", "Bonfire deck", "Mountain-facing rooms"),
                photoIndex = 7
            ),
            baseRate = 6500,
            units = 4
        ),
        HotelSeed(
            Hotel(
                id = "htl-rishikesh",
                name = "Ganga Riverside Retreat",
                city = "Rishikesh",
                country = "India",
                address = "Tapovan, Laxman Jhula Road",
                description = "A riverside retreat above the Ganga with a yoga deck, a cafe " +
                    "overlooking Laxman Jhula and the evening Ganga Aarti a short walk away.",
                stars = 4,
                rating = 4.6f,
                reviewCount = 728,
                amenities = listOf(Amenity.WIFI, Amenity.BREAKFAST, Amenity.PARKING),
                highlights = listOf("Riverside yoga deck", "Walk to the Ganga Aarti", "Cafe with river views"),
                photoIndex = 8
            ),
            baseRate = 6000,
            units = 5
        ),
        HotelSeed(
            Hotel(
                id = "htl-varanasi",
                name = "Ghat View Heritage Hotel",
                city = "Varanasi",
                country = "India",
                address = "Assi Ghat Road",
                description = "A restored haveli overlooking Assi Ghat, with rooftop views of " +
                    "the sunrise boat traffic and the evening aarti on the Ganga.",
                stars = 4,
                rating = 4.5f,
                reviewCount = 693,
                amenities = listOf(Amenity.WIFI, Amenity.BREAKFAST, Amenity.AIRPORT_SHUTTLE),
                highlights = listOf("Rooftop ghat views", "Sunrise boat rides", "Walk to the evening aarti"),
                photoIndex = 9
            ),
            baseRate = 6800,
            units = 4
        ),
        HotelSeed(
            Hotel(
                id = "htl-amritsar",
                name = "Golden Temple View Hotel",
                city = "Amritsar",
                country = "India",
                address = "12 Golden Temple Road",
                description = "A five-minute walk from the Golden Temple, with rooftop dining " +
                    "over the old city and complimentary transfers for the langar hall.",
                stars = 4,
                rating = 4.6f,
                reviewCount = 1204,
                amenities = listOf(Amenity.WIFI, Amenity.BREAKFAST, Amenity.PARKING, Amenity.AIRPORT_SHUTTLE),
                highlights = listOf("5 min to the Golden Temple", "Rooftop dining", "Old city views"),
                photoIndex = 10
            ),
            baseRate = 6200,
            units = 5
        ),
        HotelSeed(
            Hotel(
                id = "htl-pondicherry",
                name = "French Quarter Boutique Hotel",
                city = "Pondicherry",
                country = "India",
                address = "14 Rue Romain Rolland",
                description = "A pastel colonial townhouse in the French Quarter, with a courtyard " +
                    "cafe, bicycles for the promenade and the beach a short ride away.",
                stars = 4,
                rating = 4.6f,
                reviewCount = 589,
                amenities = listOf(Amenity.WIFI, Amenity.BREAKFAST, Amenity.PET_FRIENDLY),
                highlights = listOf("French Quarter courtyard", "Free bicycle hire", "10 min to the beach"),
                photoIndex = 11
            ),
            baseRate = 7200,
            units = 3
        )
    )

    private val typeMultipliers = mapOf(
        RoomType.STANDARD to 100,
        RoomType.DELUXE to 150,
        RoomType.SUITE to 240
    )

    private val typeDetails = mapOf(
        RoomType.STANDARD to Triple("1 queen bed", 24, 0),
        RoomType.DELUXE to Triple("1 king bed", 34, 0),
        RoomType.SUITE to Triple("1 king bed + sofa bed", 56, -1)
    )

    fun hotels(): List<Hotel> = seeds.map { it.hotel }

    fun offers(): List<RoomOffer> = seeds.flatMap { seed ->
        RoomType.entries.map { type ->
            val (beds, size, unitAdjustment) = typeDetails.getValue(type)
            RoomOffer(
                id = "${seed.hotel.id}-${type.name.lowercase()}",
                hotelId = seed.hotel.id,
                type = type,
                nightlyRate = seed.baseRate * typeMultipliers.getValue(type) / 100 / 100 * 100,
                units = (seed.units + unitAdjustment).coerceAtLeast(5),
                beds = beds,
                sizeSqm = size
            )
        }
    }

    fun reviews(today: LocalDate): List<Review> = listOf(
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
    )

    fun bookings(today: LocalDate): List<Booking> = listOf(
        booking("bk-1001", "htl-goa-deluxe", 13500, today.plusDays(12), today.plusDays(16), 2, BookingStatus.CONFIRMED, "Sea-facing room if possible", today.minusDays(6), true, 10, false),
        booking("bk-1002", "htl-delhi-standard", 11000, today.plusDays(30), today.plusDays(33), 1, BookingStatus.CONFIRMED, "", today.minusDays(2), false, 0, true),
        booking("bk-1003", "htl-jaipur-suite", 18000, today.minusDays(40), today.minusDays(37), 3, BookingStatus.CONFIRMED, "Rooftop dinner on the first night", today.minusDays(70), true, 0, false),
        booking("bk-1004", "htl-mumbai-standard", 12000, today.minusDays(90), today.minusDays(88), 2, BookingStatus.CONFIRMED, "", today.minusDays(100), false, 0, false),
        booking("bk-1005", "htl-udaipur-deluxe", 21000, today.plusDays(20), today.plusDays(22), 2, BookingStatus.CANCELLED, "", today.minusDays(14), false, 0, false)
    )

    private fun review(
        id: String,
        hotelId: String,
        guestName: String,
        rating: Float,
        comment: String,
        date: LocalDate
    ) = Review(id, hotelId, "", guestName, rating, comment, date)

    private fun booking(
        id: String,
        roomId: String,
        nightlyRate: Int,
        checkIn: LocalDate,
        checkOut: LocalDate,
        guests: Int,
        status: BookingStatus,
        specialRequests: String,
        bookedOn: LocalDate,
        breakfastIncluded: Boolean,
        promoPercent: Int,
        payAtHotel: Boolean
    ) = Booking(
        id = id,
        hotelId = roomId.substringBeforeLast('-'),
        roomId = roomId,
        guestName = profile.name,
        guestPhone = profile.phone,
        guestEmail = profile.email,
        checkIn = checkIn,
        checkOut = checkOut,
        guests = guests,
        status = status,
        specialRequests = specialRequests,
        bookedOn = bookedOn,
        nightlyRate = nightlyRate,
        breakfastIncluded = breakfastIncluded,
        promoPercent = promoPercent,
        payAtHotel = payAtHotel
    )
}
