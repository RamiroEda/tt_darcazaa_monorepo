package mx.ipn.upiiz.darcazaa.data.hilt

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.socket.client.IO
import mx.ipn.upiiz.darcazaa.data.data_providers.LocalDatabase
import mx.ipn.upiiz.darcazaa.data.data_providers.ioOptions
import mx.ipn.upiiz.darcazaa.data.models.PreferenceKeys
import mx.ipn.upiiz.darcazaa.data.models.SocketProvider
import mx.ipn.upiiz.darcazaa.data.models.UserPreferences
import mx.ipn.upiiz.darcazaa.data.repositories.ChargingStationRepository
import mx.ipn.upiiz.darcazaa.data.repositories.ConnectionRepository
import mx.ipn.upiiz.darcazaa.data.repositories.ConnectionSocketIORepository
import mx.ipn.upiiz.darcazaa.data.repositories.DroneSocketIORepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Singleton
    @Provides
    fun providePreferences(app: Application): UserPreferences = UserPreferences(app)

    @Singleton
    @Provides
    fun provideSocket(
        preferences: UserPreferences
    ): SocketProvider = SocketProvider(
        IO.socket("ws://${preferences.get(PreferenceKeys.Url, "192.168.1.1")}/routines", ioOptions)
    )

    @Singleton
    @Provides
    fun provideConnectionRepository(
        socket: SocketProvider,
        preferences: UserPreferences
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