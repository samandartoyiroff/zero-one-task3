package uz.thirdTask.auth

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AuthClientRepository : MongoRepository<AuthClient, String> {
    fun findByClientId(clientId: String?): AuthClient?
}

@Repository
interface AccessTokenRepository : MongoRepository<AccessToken, String> {
    fun findByTokenId(tokenId: String): AccessToken?
    fun findByAuthenticationId(authenticationId: String): AccessToken?
    fun deleteByTokenId(tokenId: String)
    fun deleteByRefreshToken(refreshToken: String)
    fun findAllByClientIdAndUsername(clientId: String, username: String): List<AccessToken>
    fun findAllByClientId(clientId: String): List<AccessToken>
}

@Repository
interface RefreshTokenRepository : MongoRepository<RefreshToken, String> {
    fun findByTokenId(tokenId: String): RefreshToken?
    fun deleteByTokenId(tokenId: String)
    fun deleteByTokenValue(tokenValue: String)
}