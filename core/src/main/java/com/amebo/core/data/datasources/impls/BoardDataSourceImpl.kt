package com.amebo.core.data.datasources.impls

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import com.amebo.core.Database
import com.amebo.core.apis.TopicListApi
import com.amebo.core.common.extensions.awaitResult
import com.amebo.core.crawler.topicList.parseFollowedBoards
import com.amebo.core.data.CoroutineContextProvider
import com.amebo.core.data.datasources.BoardDataSource
import com.amebo.core.domain.Board
import com.amebo.core.domain.ErrorResponse
import com.amebo.core.domain.TopicListSorts
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

internal class BoardDataSourceImpl @Inject constructor(
    private val db: Database,
    private val src: SourceDatabase,
    private val topicListApi: TopicListApi,
    private val context: CoroutineContextProvider
) :
    BoardDataSource {

    override suspend fun initialize() {
        withContext(context.IO) {
            db.transaction {
                src.fetchBoards().forEach {
                    if (db.boardQueries.selectById(it.id.toLong()).executeAsOneOrNull() == null) {
                        db.boardQueries.insert(it.name, it.url, it.id.toLong())
                    }
                }
            }
        }
    }

    override suspend fun loadAll(): List<Board> {
        return withContext(context.IO) {
            db.boardQueries.selectAll { name, slug, id -> Board(name, slug, id.toInt()) }
                .executeAsList()
        }
    }

    override suspend fun loadNairalandPicks(): List<Board> {
        return withContext(context.IO) {
            db.boardQueries.selectNairalandPicks { name, slug, id -> Board(name, slug, id.toInt()) }
                .executeAsList()
        }
    }

    override suspend fun selectFromSlug(boardSlug: String): Board {
        return withContext(context.IO) {
            db.boardQueries.select(boardSlug) { name, slug, id ->
                Board(name, slug, id.toInt())
            }.executeAsOne()
        }
    }

    override suspend fun loadFollowedFromDisk(userSlug: String): List<Board> {
        return withContext(context.IO) {
            db.followedBoardQueries.selectAll(userSlug) { name, slug, id, _ ->
                Board(name, slug, id.toInt())
            }.executeAsList()
        }
    }

    override suspend fun fetchFollowedBoards(): Result<List<Board>, ErrorResponse> =
        withContext(context.IO) {
            // the sort doesn't really matter, and the page number as long as it's valid
            topicListApi.fetchFollowedBoardTopics(TopicListSorts.CREATION.value, 0)
                .awaitResult { document ->
                    document.parseFollowedBoards(0)
                        .map { it.boards.map { itt -> itt.first }.toList() }
                }
        }


    override suspend fun loadRecent(): List<Board> {
        return withContext(context.IO) {
            db.recentBoardQueries.selectNRecent(5) { name, slug, id ->
                Board(
                    name,
                    slug,
                    id.toInt()
                )
            }.executeAsList()
        }
    }

    override suspend fun updateFollowedBoards(userSlug: String, followedBoards: List<Board>): Unit =
        withContext(context.IO) {
            db.transaction {
                db.followedBoardQueries.deleteAllByAccount(userSlug)
                followedBoards.forEach {
                    db.followedBoardQueries.insert(it.url, userSlug, 1)
                }
            }
        }

    override suspend fun search(query: String): List<Board> {
        return withContext(context.IO) {
            db.boardQueries.findLike(query) { name, slug, id -> Board(name, slug, id.toInt()) }
                .executeAsList()
        }
    }

    class SourceDatabase @Inject constructor(private val context: Context) {
        private val location: String = "${context.filesDir.absolutePath}/databases/"
        private val databaseName: String = "database.db"
        private val path: String = {
            val file = File(location)
            if (!file.exists()) {
                file.mkdir()
            }
            location + databaseName
        }()

        fun fetchBoards(): List<Board> {
            prepareDatabase()
            return fetchBoardsFromTable("boards") + fetchBoardsFromTable("boards_more")
        }

        private fun prepareDatabase() {
            if (!isDatabaseExists()) {
                copyDataBase()
            }
        }

        private fun isDatabaseExists(): Boolean {
            return try {
                val db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)
                db.close()
                true
            } catch (e: SQLiteException) {
                false
            }
        }

        private fun copyDataBase() {
            val input = context.assets.open(databaseName)
            val output = FileOutputStream(path)
            val buffer = ByteArray(10)
            var length: Int
            while (input.read(buffer).also { length = it } > 0) {
                output.write(buffer, 0, length)
            }
            output.flush()
            output.close()
            input.close()
        }

        private fun fetchBoardsFromTable(table: String): List<Board> {
            val boards = mutableListOf<Board>()
            val db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)
            val c = db.rawQuery("SELECT * FROM $table", emptyArray())
            if (c.moveToFirst()) {
                do {
                    val nameIdx = c.getColumnIndex("name")
                    val slugIdx = c.getColumnIndex("slug")
                    val idIdx = c.getColumnIndex("id")
                    val name = c.getString(nameIdx)
                    val slug = c.getString(slugIdx)
                    val id = c.getInt(idIdx)
                    boards.add(Board(name, slug, id))
                } while (c.moveToNext())
            }
            c.close()
            db.close()

            return boards
        }
    }
}