package com.radioisaac.data

data class BrazilRegion(
    val display: String,       // chip label shown to user
    val stateName: String,     // for RadioBrowser bystate query
    val stateCode: String,     // for radio.garden fallback filter
    val rgCity: String? = null // if set, radio.garden filters by city name
)
