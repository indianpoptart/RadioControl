/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nikhilparanjape.radiocontrol.util

import org.json.JSONException
import org.json.JSONObject

/**
 * Represents an in-app product's listing details.
 */
class SkuDetails @Throws(JSONException::class)
constructor(internal var mItemType: String, internal var mJson: String) {
    var sku: String
        internal set
    var type: String
        internal set
    var price: String
        internal set
    var title: String
        internal set
    var description: String
        internal set

    @Throws(JSONException::class)
    constructor(jsonSkuDetails: String) : this(IabHelper.ITEM_TYPE_INAPP, jsonSkuDetails) {
    }

    init {
        val o = JSONObject(mJson)
        sku = o.optString("productId")
        type = o.optString("type")
        price = o.optString("price")
        title = o.optString("title")
        description = o.optString("description")
    }

    override fun toString(): String {
        return "SkuDetails:$mJson"
    }
}
