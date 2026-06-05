package com.example.swiftward.data.local


import com.swiftward.data.model.*

object MockData {

    fun getAll(): List<Hospital> = listOf(

        Hospital(
            id = "h1", name = "Bir Hospital",
            address = "Mahabauddha, Kathmandu",
            latitude = 27.7041, longitude = 85.3145,
            phone = "+977-1-4221119", isOpen24x7 = true,
            wards = listOf(
                Ward("w1",  WardType.GENERAL,    totalBeds = 120, freeBeds = 18),
                Ward("w2",  WardType.ICU,         totalBeds = 20,  freeBeds = 4),
                Ward("w3",  WardType.HDU,         totalBeds = 15,  freeBeds = 2),
                Ward("w4",  WardType.EMERGENCY,   totalBeds = 30,  freeBeds = 8),
                Ward("w5",  WardType.NEUROLOGY,   totalBeds = 25,  freeBeds = 5),
                Ward("w6",  WardType.ORTHOPEDIC,  totalBeds = 20,  freeBeds = 3)
            )
        ),

        Hospital(
            id = "h2", name = "Tribhuvan University Teaching Hospital",
            address = "Maharajgunj, Kathmandu",
            latitude = 27.7369, longitude = 85.3306,
            phone = "+977-1-4412303", isOpen24x7 = true,
            wards = listOf(
                Ward("w7",  WardType.GENERAL,    totalBeds = 200, freeBeds = 9),
                Ward("w8",  WardType.ICU,         totalBeds = 30,  freeBeds = 2),
                Ward("w9",  WardType.PEDIATRIC,   totalBeds = 40,  freeBeds = 5),
                Ward("w10", WardType.MATERNITY,   totalBeds = 35,  freeBeds = 3),
                Ward("w11", WardType.CARDIAC,     totalBeds = 20,  freeBeds = 1),
                Ward("w12", WardType.NEUROLOGY,   totalBeds = 15,  freeBeds = 2)
            )
        ),

        Hospital(
            id = "h3", name = "Patan Hospital",
            address = "Lagankhel, Lalitpur",
            latitude = 27.6772, longitude = 85.3215,
            phone = "+977-1-5522266", isOpen24x7 = true,
            wards = listOf(
                Ward("w13", WardType.GENERAL,   totalBeds = 100, freeBeds = 0),
                Ward("w14", WardType.ICU,        totalBeds = 15,  freeBeds = 0),
                Ward("w15", WardType.MATERNITY,  totalBeds = 30,  freeBeds = 0)
            )
        ),

        Hospital(
            id = "h4", name = "Norvic International Hospital",
            address = "Thapathali, Kathmandu",
            latitude = 27.6949, longitude = 85.3195,
            phone = "+977-1-5970032", isOpen24x7 = true,
            wards = listOf(
                Ward("w16", WardType.GENERAL,    totalBeds = 60,  freeBeds = 12),
                Ward("w17", WardType.ICU,         totalBeds = 12,  freeBeds = 3),
                Ward("w18", WardType.HDU,         totalBeds = 10,  freeBeds = 1),
                Ward("w19", WardType.CARDIAC,     totalBeds = 15,  freeBeds = 2)
            )
        ),

        Hospital(
            id = "h5", name = "Grande International Hospital",
            address = "Dhapasi, Kathmandu",
            latitude = 27.7568, longitude = 85.3268,
            phone = "+977-1-5159266", isOpen24x7 = true,
            wards = listOf(
                Ward("w20", WardType.GENERAL,    totalBeds = 80,  freeBeds = 7),
                Ward("w21", WardType.ICU,         totalBeds = 18,  freeBeds = 5),
                Ward("w22", WardType.PEDIATRIC,   totalBeds = 20,  freeBeds = 2),
                Ward("w23", WardType.EMERGENCY,   totalBeds = 15,  freeBeds = 2),
                Ward("w24", WardType.BURN,        totalBeds = 10,  freeBeds = 1)
            )
        ),

        Hospital(
            id = "h6", name = "Mediciti Hospital",
            address = "Bhaisepati, Lalitpur",
            latitude = 27.6600, longitude = 85.3100,
            phone = "+977-1-5200777", isOpen24x7 = true,
            wards = listOf(
                Ward("w25", WardType.GENERAL,    totalBeds = 50,  freeBeds = 14),
                Ward("w26", WardType.ICU,         totalBeds = 10,  freeBeds = 4),
                Ward("w27", WardType.MATERNITY,   totalBeds = 20,  freeBeds = 6),
                Ward("w28", WardType.PEDIATRIC,   totalBeds = 15,  freeBeds = 3)
            )
        ),

        Hospital(
            id = "h7", name = "B&B Hospital",
            address = "Gwarko, Lalitpur",
            latitude = 27.6717, longitude = 85.3371,
            phone = "+977-1-5199999", isOpen24x7 = true,
            wards = listOf(
                Ward("w29", WardType.GENERAL,    totalBeds = 70,  freeBeds = 11),
                Ward("w30", WardType.ICU,         totalBeds = 12,  freeBeds = 0),
                Ward("w31", WardType.ORTHOPEDIC,  totalBeds = 20,  freeBeds = 4),
                Ward("w32", WardType.CARDIAC,     totalBeds = 10,  freeBeds = 2)
            )
        ),

        Hospital(
            id = "h8", name = "Nepal Medical College",
            address = "Attarkhel, Jorpati",
            latitude = 27.7450, longitude = 85.3700,
            phone = "+977-1-4913000", isOpen24x7 = true,
            wards = listOf(
                Ward("w33", WardType.GENERAL,    totalBeds = 90,  freeBeds = 20),
                Ward("w34", WardType.ICU,         totalBeds = 16,  freeBeds = 6),
                Ward("w35", WardType.MATERNITY,   totalBeds = 25,  freeBeds = 5),
                Ward("w36", WardType.PEDIATRIC,   totalBeds = 18,  freeBeds = 3),
                Ward("w37", WardType.NEUROLOGY,   totalBeds = 12,  freeBeds = 2)
            )
        ),

        Hospital(
            id = "h9", name = "Civil Service Hospital",
            address = "Minbhawan, Kathmandu",
            latitude = 27.7000, longitude = 85.3350,
            phone = "+977-1-4243303", isOpen24x7 = true,
            wards = listOf(
                Ward("w38", WardType.GENERAL,    totalBeds = 80,  freeBeds = 8),
                Ward("w39", WardType.ICU,         totalBeds = 10,  freeBeds = 1),
                Ward("w40", WardType.HDU,         totalBeds = 8,   freeBeds = 0),
                Ward("w41", WardType.ORTHOPEDIC,  totalBeds = 15,  freeBeds = 2)
            )
        ),

        Hospital(
            id = "h10", name = "Kantipur Hospital",
            address = "Tinkune, Kathmandu",
            latitude = 27.6927, longitude = 85.3510,
            phone = "+977-1-4469026", isOpen24x7 = true,
            wards = listOf(
                Ward("w42", WardType.GENERAL,    totalBeds = 60,  freeBeds = 5),
                Ward("w43", WardType.CARDIAC,     totalBeds = 12,  freeBeds = 3),
                Ward("w44", WardType.NEUROLOGY,   totalBeds = 10,  freeBeds = 1),
                Ward("w45", WardType.BURN,        totalBeds = 8,   freeBeds = 2)
            )
        ),

        Hospital(
            id = "h11", name = "Om Hospital & Research Centre",
            address = "Chabahil, Kathmandu",
            latitude = 27.7210, longitude = 85.3491,
            phone = "+977-1-4476940", isOpen24x7 = true,
            wards = listOf(
                Ward("w46", WardType.GENERAL,    totalBeds = 45,  freeBeds = 10),
                Ward("w47", WardType.ICU,         totalBeds = 8,   freeBeds = 2),
                Ward("w48", WardType.MATERNITY,   totalBeds = 15,  freeBeds = 4)
            )
        ),

        Hospital(
            id = "h12", name = "Scheer Memorial Hospital",
            address = "Banepa, Kavrepalanchok",
            latitude = 27.6300, longitude = 85.5200,
            phone = "+977-11-661234", isOpen24x7 = true,
            wards = listOf(
                Ward("w49", WardType.GENERAL,    totalBeds = 55,  freeBeds = 15),
                Ward("w50", WardType.ICU,         totalBeds = 8,   freeBeds = 3),
                Ward("w51", WardType.PEDIATRIC,   totalBeds = 12,  freeBeds = 4),
                Ward("w52", WardType.MATERNITY,   totalBeds = 10,  freeBeds = 2)
            )
        )
    )

    fun getDoctors(): Map<String, String> = mapOf(
        "h1" to "Dr. Sita Sharma",
        "h2" to "Dr. Ramesh Adhikari",
        "h3" to "Dr. Priya Thapa",
        "h4" to "Dr. Bikash Karki",
        "h5" to "Dr. Anita Shrestha"
    )

    fun createMockBooking(request: BookingRequest, hospital: Hospital): Booking = Booking(
        bookingId = "SW-${(1000..9999).random()}",
        hospitalId = hospital.id,
        hospitalName = hospital.name,
        wardType = request.wardType,
        patient = request.patient,
        condition = request.condition,
        etaMinutes = request.etaMinutes,
        notes = request.notes,
        isEmergency = request.isEmergency,
        status = BookingStatus.CONFIRMED,
        assignedDoctor = getDoctors()[hospital.id] ?: "Dr. On-duty",
        hospitalPhone = hospital.phone,
        feePaid = 0,
    )
}