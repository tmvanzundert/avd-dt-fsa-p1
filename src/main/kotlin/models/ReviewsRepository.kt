package com.example.models

interface ReviewsRepository : CrudRepository<Review, Long>  {
}

class ReviewsDao :
    CrudDAO<Review, Long, ReviewTable>(ReviewTable),
    ReviewsRepository {

    override fun getEntity(row: org.jetbrains.exposed.v1.core.ResultRow): Review {
        return Review(
            id = row[ReviewTable.id],
            renterId = row[ReviewTable.renterId],
            ownerId = row[ReviewTable.ownerId],
            rating = row[ReviewTable.rating],
            comment = row[ReviewTable.comment],
            reviewDate = row[ReviewTable.reviewDate],
        )
    }

    override fun createEntity(entity: Review, statement: org.jetbrains.exposed.v1.core.statements.UpdateBuilder<Int>) {
        statement[ReviewTable.renterId] = entity.renterId
        statement[ReviewTable.ownerId] = entity.ownerId
        statement[ReviewTable.rating] = entity.rating
        statement[ReviewTable.comment] = entity.comment
        statement[ReviewTable.reviewDate] = entity.reviewDate
    }
}