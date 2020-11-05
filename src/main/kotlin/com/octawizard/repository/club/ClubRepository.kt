package com.octawizard.repository.club

import com.octawizard.domain.model.Club
import java.util.*

interface ClubRepository {

    fun getClub(id: UUID): Club?

}
