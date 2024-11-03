
import android.util.Log

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import kotlinx.serialization.Serializable


// Определите модель данных
@Serializable
data class Item(val id: Int? = null, val name: String)

// Ваш класс клиента API
object ApiClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()  // Этот метод устанавливает сериализацию JSON с использованием kotlinx.serialization
        }
    }

    // Получение всех элементов
    suspend fun fetchItems(host: String, api: String): HttpResponse {
        val cli = client.get("http://${host}:1445/${api}/work/old")
        if (cli.status.value ==404)
        {
            return  client.get("http://${host}:1445/${api}/work/new")
        }
        return cli
    }
    // Получение всех элементов
    suspend fun fetchItemsNew(host: String, api: String): HttpResponse {

        return client.get("http://${host}:1445/${api}/work/new")
    }
    // Получение состояние элементов
    suspend fun isOpen(host: String, api: String): Boolean {
        val cli = client.get("http://${host}:1445/${api}/work/old")
        return "${cli.status}" !="404"
    }

    // Получение всех элементов
    suspend fun editEl(host: String, api: String,state:Int,num:Int): HttpResponse {

//        Log.d("tag", client.get("http://10.114.7.93:1445").toString())
        return client.get("http://${host}:1445/${api}/work/${state}/${num}")
    }

    // Создание нового элемента
    suspend fun createItem(name: String): HttpResponse {
        return client.post("http://10.114.7.93:1445/work") {
            contentType(ContentType.Application.Json)

        }
    }



}
