package com.example.ulaugh.di

import com.example.ulaugh.api.AuthInterceptor
import com.example.ulaugh.api.LaughAPI
import com.example.ulaugh.api.UserApi
import com.example.ulaugh.repository.UserFirebaseRepository
import com.example.ulaugh.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {
    @Singleton
    @Provides
    fun providesRetrofit(): Retrofit.Builder {
        return Retrofit.Builder().baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
    }

//    @Singleton
//    @Provides
//    fun provideOkHttpClient(interceptor: AuthInterceptor): OkHttpClient {
//        return OkHttpClient.Builder().addInterceptor(interceptor).build()
//    }

    @Singleton
    @Provides
    fun providesUserAPI(retrofitBuilder: Retrofit.Builder): UserApi {
        return retrofitBuilder.build().create(UserApi::class.java)
    }

//    @Singleton
//    @Provides
//    fun providesFirebaseApi(userFirebaseRepository: UserFirebaseRepository):

//    @Singleton
//    @Provides
//    fun providesLaughAPI(retrofitBuilder: Retrofit.Builder, okHttpClient: OkHttpClient): LaughAPI {
//        return retrofitBuilder.client(okHttpClient).build().create(LaughAPI::class.java)
//    }

}