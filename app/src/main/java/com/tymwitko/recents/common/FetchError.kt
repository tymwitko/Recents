package com.tymwitko.recents.common

import com.tymwitko.recents.common.dataclasses.App

sealed class FetchError : Error {
  object FullEmpty: FetchError()
  data class FilteredEmpty(val fullList: List<App>): FetchError()
}
