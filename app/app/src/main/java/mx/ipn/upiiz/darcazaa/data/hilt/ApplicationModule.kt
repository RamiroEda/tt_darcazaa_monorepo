package mx.ipn.upiiz.darcazaa.data.hilt

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.socket.client.IO
import mx.ipn.upiiz.darcazaa.data.data_providers.LocalDatabase
import mx.ipn.upiiz.darcazaa.data.data_providers.ioOptions
import mx.ipn.upiiz.darcazaa.data.models.SocketProvider
import mx.ipn.upiiz.darcazaa.data.repositories.ConnectionRepository
import mx.ipn.upiiz.darcazaa.data.repositories.ConnectionSocketIORepository
import mx.ipn.upiiz.darcazaa.data.repositories.ChargingStationRepository
import mx.ipn.upiiz.darcazaa.data.repositories.DroneSocketIORepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Singleton
    @Provides
    fun providePreferences(app: Application): SharedPreferences = app.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideSocket(
        preferences: SharedPreferences
    ): SocketProvider = SocketProvider(
        IO.socket("ws://${preferences.getString("url", "192.168.1.1")}/routines", ioOptions)
    )

    @Singleton
    @Provides
    fun provideConnectionRepository(
        socket: SocketProvider,
        preferences: SharedPreferences
    ): ConnectionRepository = ConnectionSocketIORepository(socket, preferences)

    @Singleton
    @Provides
    fun provideDroneRepository(
        socket: SocketProvider,
        localDatabase: LocalDatabase
    ): ChargingStationRepository = DroneSocketIORepository(socket, localDatabase)

    @Singleton
    @Provides
    fun provideLocalDatabase(
        app: Application
    ) = LocalDatabase.getInstance(app)
}