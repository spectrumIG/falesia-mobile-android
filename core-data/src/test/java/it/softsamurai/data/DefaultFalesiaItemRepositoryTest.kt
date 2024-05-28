/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.softsamurai.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import it.softsamurai.core.data.DefaultFalesiaItemRepository
import it.softsamurai.core.database.FalesiaItem
import it.softsamurai.core.database.FalesiaItemDao

/**
 * Unit tests for [DefaultFalesiaItemRepository].
 */
@OptIn(ExperimentalCoroutinesApi::class) // TODO: Remove when stable
class DefaultFalesiaItemRepositoryTest {

    @Test
    fun falesiaItems_newItemSaved_itemIsReturned() = runTest {
        val repository = it.softsamurai.core.data.DefaultFalesiaItemRepository(FakeFalesiaItemDao())

        repository.add("Repository")

        assertEquals(repository.falesiaItems.first().size, 1)
    }

}

private class FakeFalesiaItemDao : FalesiaItemDao {

    private val data = mutableListOf<FalesiaItem>()

    override fun getFalesiaItems(): Flow<List<FalesiaItem>> = flow {
        emit(data)
    }

    override suspend fun insertFalesiaItem(item: FalesiaItem) {
        data.add(0, item)
    }
}
