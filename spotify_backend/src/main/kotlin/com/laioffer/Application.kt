package com.laioffer

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class Song (
    val name: String,
    val lyric: String,
    val src: String,
    val length: String
)

@Serializable
data class Playlist (
    val id: Int,
    val songs: List<Song>
)

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

// extension
fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
        })
    }
    // TODO: adding the routing configuration here

    // restful api: get, put, post, delete
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/feed") {
            // optional/nullable chaining
            val jsonString: String? = this::class.java.classLoader.getResource("feed.json")?.readText()
            call.respondText(jsonString ?: "null")
        }

        get("/playlists") {
            val jsonString = this::class.java.classLoader.getResource("playlists.json")?.readText()
            call.respondText(jsonString ?: "null")
        }

        get("/playlist/{id}") {
            val jsonString = this::class.java.classLoader.getResource("playlists.json")?.readText()
            jsonString?.let {
                val playlists: List<Playlist> = Json.decodeFromString(ListSerializer(Playlist.serializer()), it)
                // operator
                val id = call.parameters["id"]
                val playlist: Playlist? = playlists.find { item: Playlist -> item.id.toString() == id  }
                call.respondNullable(playlist)
            } ?: call.respondText("null")
        }

        // http://0.0.0.0:8080/songs/solo.mp3
        static("/") {
            staticBasePackage = "static"
            static("songs") {
                resources("songs")
            }
        }
    }
}