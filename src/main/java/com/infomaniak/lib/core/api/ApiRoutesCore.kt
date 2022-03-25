package com.infomaniak.lib.core.api

import com.infomaniak.lib.core.BuildConfig.INFOMANIAK_API

object ApiRoutesCore {

    fun getUserProfile() = "${INFOMANIAK_API}profile"

}