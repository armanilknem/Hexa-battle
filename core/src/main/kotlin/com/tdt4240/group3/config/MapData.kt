package com.tdt4240.group3.config

object MapData {

    /**
     * Hex coordinates (q, r) of water tiles on the default map.
     * Used to exclude these positions from city and capital placement.
     */
    val WATER_TILES = setOf(
        2 to 9, 3 to 9, 2 to 8, 3 to 8, 2 to 7, 3 to 7, 4 to 7,
        8 to 14, 9 to 14, 9 to 13,
        10 to 5, 10 to 4, 11 to 4, 10 to 3, 11 to 3, 12 to 3,
        6 to 0, 7 to 0, 8 to 0, 9 to 0, 10 to 0, 11 to 0
    )

    val CITY_NAMES = listOf(
        "Aakrehavn", "Aalesund", "Alta", "Aandalsnes", "Arendal", "Aas", "Aasgaardstrand",
        "Askim", "Askoey", "Bodoe", "Brevik", "Broennoeysund", "Brumunddal", "Bryne",
        "Drammen", "Droebak", "Egersund", "Eidsvoll", "Elverum", "Fagernes", "Fauske",
        "Finnsnes", "Flekkefjord", "Floroe", "Foerde", "Fosnavaaag", "Fredrikstad", "Gjoevik",
        "Grimstad", "Halden", "Hamar", "Hammerfest", "Harstad", "Haugesund", "Hokksund",
        "Holmestrand", "Hoenefoss", "Honningsvaaag", "Horten", "Jessheim", "Joerpeland",
        "Kirkenes", "Kongsberg", "Kongsvinger", "Kopervik", "Krageroe", "Kristiansand",
        "Kristiansund", "Langesund", "Larvik", "Leknes", "Levanger", "Lillehammer",
        "Lillesand", "Lillestrøm", "Lyngdal", "Mandal", "Moelv", "Mo i Rana", "Molde",
        "Mosjoen", "Moss", "Naeroeay", "Namsos", "Narvik", "Nesoddtangen", "Notodden",
        "Odda", "Orkanger", "Otta", "Porsgrunn", "Raufoss", "Risoer", "Rjukan",
        "Sandefjord", "Sandnes", "Sandnessjoen", "Sandvika", "Sauda", "Ski", "Skien",
        "Sortland", "Stavanger", "Stavern", "Steinkjer", "Stjoerdal", "Stokmarknes",
        "Stord", "Svelvik", "Svolvaer", "Toensberg", "Tromsoe", "Trondheim", "Tvedestrand",
        "Ulsteinvik", "Vadsoe", "Vardoe", "Farsund", "Maaloey", "Verdal", "Vestby", "Vinstra"
    )

    val CAPITAL_NAMES = listOf(
        "London", "Paris", "Berlin", "Rome", "Madrid",
        "Tokyo", "Washington", "Beijing", "Cairo", "Brasilia"
    )
}
