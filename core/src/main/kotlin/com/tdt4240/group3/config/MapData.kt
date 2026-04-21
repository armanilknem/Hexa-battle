package com.tdt4240.group3.config

object MapData {

    /**
     * Hex coordinates (q, r) of water tiles on the default map.
     * Used to exclude these positions from city and capital placement.
     */
    val WATER_TILES = setOf(
        1 to 7, 1 to 8, 1 to 9,
        2 to 7, 2 to 8, 2 to 9, 3 to 7,

        11 to 3, 11 to 4, 11 to 5, 12 to 3,
        12 to 3, 12 to 4, 12 to 5,
        13 to 3, 13 to 4, 14 to 3
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
