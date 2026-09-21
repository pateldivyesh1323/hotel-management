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
            baseRate = 145,
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
            baseRate = 110,
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
            baseRate = 95,
            units = 3
        ),
        HotelSeed(
            Hotel(
                id = "htl-london",
                name = "The Kensington Row",
                city = "London",
                country = "United Kingdom",
                address = "27 Queen's Gate",
                description = "A townhouse hotel steps from the museums and Hyde Park, with " +
                    "a quiet library bar and rooms dressed in tailored British fabrics.",
                stars = 5,
                rating = 4.8f,
                reviewCount = 2107,
                amenities = listOf(Amenity.WIFI, Amenity.GYM, Amenity.SPA, Amenity.BREAKFAST),
                highlights = listOf("Next to the V&A and Science Museum", "Library bar", "Concierge theatre bookings"),
                photoIndex = 3
            ),
            baseRate = 240,
            units = 3
        ),
        HotelSeed(
            Hotel(
                id = "htl-lisbon",
                name = "Alfama Terrace Hotel",
                city = "Lisbon",
                country = "Portugal",
                address = "Rua das Portas do Sol 9",
                description = "Tiled façades and a terrace above the Tagus: a boutique stay in " +
                    "the oldest quarter of Lisbon, close to the tram 28 line and the fado houses.",
                stars = 4,
                rating = 4.4f,
                reviewCount = 691,
                amenities = listOf(Amenity.WIFI, Amenity.BREAKFAST, Amenity.PET_FRIENDLY),
                highlights = listOf("Tram 28 at the door", "Terrace with river views", "Fado nights on Fridays"),
                photoIndex = 4
            ),
            baseRate = 130,
            units = 3
        ),
        HotelSeed(
            Hotel(
                id = "htl-bali",
                name = "Ubud Rice Terrace Retreat",
                city = "Ubud",
                country = "Indonesia",
                address = "Jalan Raya Tegallalang",
                description = "Open-air villas above the rice paddies, with infinity pools, " +
                    "daily yoga and a spa built around the valley.",
                stars = 5,
                rating = 4.9f,
                reviewCount = 976,
                amenities = listOf(Amenity.WIFI, Amenity.POOL, Amenity.SPA, Amenity.BREAKFAST, Amenity.AIRPORT_SHUTTLE),
                highlights = listOf("Private plunge pools", "Daily yoga", "Rice-terrace walks"),
                photoIndex = 5
            ),
            baseRate = 165,
            units = 2
        ),
        HotelSeed(
            Hotel(
                id = "htl-dubai",
                name = "Palm Skyline Hotel",
                city = "Dubai",
                country = "United Arab Emirates",
                address = "Crescent Road, Palm Jumeirah",
                description = "A tower of glass on the Palm's crescent with a private beach, an infinity pool 40 floors up and a spa that stays open past midnight.",
                stars = 5,
                rating = 4.6f,
                reviewCount = 1832,
                amenities = listOf(Amenity.WIFI, Amenity.POOL, Amenity.SPA, Amenity.GYM, Amenity.BREAKFAST, Amenity.PARKING),
                highlights = listOf("Private beach", "Infinity pool on floor 40", "Late-night spa"),
                photoIndex = 6
            ),
            baseRate = 210,
            units = 5
        ),
        HotelSeed(
            Hotel(
                id = "htl-paris",
                name = "Maison Le Marais",
                city = "Paris",
                country = "France",
                address = "18 Rue des Francs Bourgeois",
                description = "A 17th-century townhouse turned boutique hotel in the Marais, with a courtyard breakfast room and galleries on every corner.",
                stars = 4,
                rating = 4.5f,
                reviewCount = 1120,
                amenities = listOf(Amenity.WIFI, Amenity.BREAKFAST, Amenity.PET_FRIENDLY),
                highlights = listOf("Courtyard breakfast", "5 min to Place des Vosges", "Metro at the corner"),
                photoIndex = 7
            ),
            baseRate = 190,
            units = 3
        ),
        HotelSeed(
            Hotel(
                id = "htl-tokyo",
                name = "Shibuya Lantern Hotel",
                city = "Tokyo",
                country = "Japan",
                address = "2-14 Udagawacho, Shibuya",
                description = "Calm, compact and impeccably run: tatami-inspired rooms, a rooftop onsen bath and the Shibuya crossing five minutes away on foot.",
                stars = 4,
                rating = 4.7f,
                reviewCount = 1547,
                amenities = listOf(Amenity.WIFI, Amenity.SPA, Amenity.GYM, Amenity.BREAKFAST, Amenity.AIRPORT_SHUTTLE),
                highlights = listOf("Rooftop onsen", "Shibuya crossing 5 min", "Airport limousine bus stop"),
                photoIndex = 8
            ),
            baseRate = 155,
            units = 4
        ),
        HotelSeed(
            Hotel(
                id = "htl-newyork",
                name = "Hudson Yards Suites",
                city = "New York",
                country = "United States",
                address = "512 West 34th Street",
                description = "Suite-only living on Manhattan's west side, with full kitchens, skyline views and the High Line as your front garden.",
                stars = 4,
                rating = 4.3f,
                reviewCount = 2264,
                amenities = listOf(Amenity.WIFI, Amenity.GYM, Amenity.PARKING, Amenity.PET_FRIENDLY),
                highlights = listOf("Kitchen in every room", "Steps from the High Line", "24-hour gym"),
                photoIndex = 9
            ),
            baseRate = 260,
            units = 4
        ),
        HotelSeed(
            Hotel(
                id = "htl-bangkok",
                name = "Chao Phraya Riverside",
                city = "Bangkok",
                country = "Thailand",
                address = "89 Charoen Krung Road",
                description = "Teak, silk and a lawn running down to the river. Take the hotel boat to the Grand Palace or stay in for a Thai massage by the pool.",
                stars = 5,
                rating = 4.8f,
                reviewCount = 1390,
                amenities = listOf(Amenity.WIFI, Amenity.POOL, Amenity.SPA, Amenity.BREAKFAST, Amenity.AIRPORT_SHUTTLE),
                highlights = listOf("Free river shuttle boat", "Riverside pool", "Thai cooking school"),
                photoIndex = 10
            ),
            baseRate = 120,
            units = 4
        ),
        HotelSeed(
            Hotel(
                id = "htl-santorini",
                name = "Caldera Blue Suites",
                city = "Santorini",
                country = "Greece",
                address = "Oia Main Street",
                description = "White-washed cave suites cut into the cliff at Oia, each with a terrace facing the caldera and the famous sunset.",
                stars = 5,
                rating = 4.9f,
                reviewCount = 803,
                amenities = listOf(Amenity.WIFI, Amenity.POOL, Amenity.BREAKFAST, Amenity.SPA),
                highlights = listOf("Sunset terraces", "Cave suites with plunge pools", "Sunset dinner service"),
                photoIndex = 11
            ),
            baseRate = 250,
            units = 2
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
                nightlyRate = seed.baseRate * typeMultipliers.getValue(type) / 100 / 5 * 5,
                units = (seed.units + unitAdjustment).coerceAtLeast(1),
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
        review("rv-7", "htl-london", "Marcus Bell", 5f, "Immaculate service and a five-minute walk to three museums.", today.minusDays(12)),
        review("rv-8", "htl-london", "Aditi Sharma", 4.5f, "Small rooms as London goes, but quiet and beautifully finished.", today.minusDays(63)),
        review("rv-9", "htl-lisbon", "Lena Fischer", 4.5f, "The terrace at dusk is worth the trip alone. Steep streets, so pack light.", today.minusDays(15)),
        review("rv-10", "htl-lisbon", "Sofia Rossi", 4f, "Charming and central. Breakfast pastries were the best of our trip.", today.minusDays(40)),
        review("rv-11", "htl-bali", "Rahul Menon", 5f, "Our villa looked over the paddies and the staff arranged everything, from yoga to a driver.", today.minusDays(7)),
        review("rv-12", "htl-bali", "James Okoro", 5f, "The most peaceful place I have stayed. The spa is exceptional.", today.minusDays(29)),
        review("rv-13", "htl-dubai", "Marcus Bell", 4.5f, "The pool at sunset is unreal and the beach is properly private. Pricey dinners.", today.minusDays(11)),
        review("rv-14", "htl-dubai", "Priya Nair", 4.5f, "Enormous rooms and excellent breakfast. The shuttle to the mall is a big plus.", today.minusDays(36)),
        review("rv-15", "htl-paris", "Lena Fischer", 5f, "Courtyard breakfast, a perfect location and staff who booked our dinners.", today.minusDays(20)),
        review("rv-16", "htl-paris", "Sofia Rossi", 4f, "Lovely, but the stairs are steep and the rooms are small.", today.minusDays(52)),
        review("rv-17", "htl-tokyo", "Rahul Menon", 5f, "Spotless, silent and the onsen on the roof is a treat after a day of walking.", today.minusDays(8)),
        review("rv-18", "htl-tokyo", "Aditi Sharma", 4.5f, "Ideal base for Shibuya. The limousine bus stop right outside made the airport easy.", today.minusDays(47)),
        review("rv-19", "htl-newyork", "James Okoro", 4f, "Great to have a kitchen and the High Line is right there. Street noise on lower floors.", today.minusDays(14)),
        review("rv-20", "htl-newyork", "Marcus Bell", 4.5f, "Suites are huge by New York standards and the gym is properly equipped.", today.minusDays(38)),
        review("rv-21", "htl-bangkok", "Priya Nair", 5f, "The river boat to the Grand Palace is a lovely touch. The best massage of my life.", today.minusDays(10)),
        review("rv-22", "htl-bangkok", "Sofia Rossi", 4.5f, "Gorgeous grounds and wonderful staff. Book the river-view rooms.", today.minusDays(33)),
        review("rv-23", "htl-santorini", "Lena Fischer", 5f, "Worth every penny. Watching the sunset from our own terrace is something I will remember.", today.minusDays(6)),
        review("rv-24", "htl-santorini", "Aditi Sharma", 5f, "Immaculate cave suite, kind hosts and a breakfast delivered to the terrace.", today.minusDays(25))
    )

    fun bookings(today: LocalDate): List<Booking> = listOf(
        booking("bk-1001", "htl-goa-deluxe", 165, today.plusDays(12), today.plusDays(16), 2, BookingStatus.CONFIRMED, "Sea-facing room if possible", today.minusDays(6), true, 10, false),
        booking("bk-1002", "htl-london-standard", 240, today.plusDays(30), today.plusDays(33), 1, BookingStatus.CONFIRMED, "", today.minusDays(2), false, 0, true),
        booking("bk-1003", "htl-jaipur-suite", 225, today.minusDays(40), today.minusDays(37), 3, BookingStatus.CONFIRMED, "Rooftop dinner on the first night", today.minusDays(70), true, 0, false),
        booking("bk-1004", "htl-mumbai-standard", 145, today.minusDays(90), today.minusDays(88), 2, BookingStatus.CONFIRMED, "", today.minusDays(100), false, 0, false),
        booking("bk-1005", "htl-lisbon-deluxe", 195, today.plusDays(20), today.plusDays(22), 2, BookingStatus.CANCELLED, "", today.minusDays(14), false, 0, false)
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
