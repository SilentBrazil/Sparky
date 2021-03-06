package com.silent.core.users

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.silent.core.utils.USERS_PATH
import com.silent.ilustriscore.core.model.BaseService

class UsersService : BaseService() {
    override val dataPath = USERS_PATH
    override var requireAuth = true

    override fun deserializeDataSnapshot(dataSnapshot: DocumentSnapshot): User {
        return dataSnapshot.toObject(User::class.java)!!.apply {
            this.id = dataSnapshot.id
        }
    }

    override fun deserializeDataSnapshot(dataSnapshot: QueryDocumentSnapshot): User {
        return dataSnapshot.toObject(User::class.java).apply {
            this.id = dataSnapshot.id
        }
    }
}