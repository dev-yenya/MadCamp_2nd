import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClass {
    private val retrofit = Retrofit.Builder()
        .baseUrl("192.249.18.201")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}