package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking as b " +
            " JOIN Item as i ON b.item.id = i.id " +
            " WHERE b.id = :bookingId " +
            " AND ( i.owner.id = :userId OR b.booker.id = :userId )")
    Optional<Booking> findByIdAndBookerOrOwner(Long bookingId, Long userId);

    @Query("SELECT b FROM Booking as b " +
            " WHERE b.booker.id = :userId " +
            " AND b.status = ru.practicum.shareit.booking.model.Status.REJECTED " +
            " ORDER BY b.start DESC ")
    List<Booking> findAllByBookerIdAndStatusRejectedOrderByStartDesc(Long userId);

    @Query("SELECT b FROM Booking as b " +
            " WHERE b.booker.id = :userId " +
            " AND b.status = ru.practicum.shareit.booking.model.Status.WAITING " +
            " ORDER BY b.start DESC ")
    List<Booking> findAllByBookerIdAndStatusWaitingOrderByStartDesc(Long userId);

    @Query("SELECT b FROM Booking as b " +
            " WHERE b.booker.id = :userId" +
            " AND b.start <= :dateTime " +
            " AND b.end >= :dateTime ")
    List<Booking> findAllByBookerIdByDateIntoPeriodOrderByStartDesc(Long userId, LocalDateTime dateTime);

    List<Booking> findAllByBookerIdAndStartIsAfterOrderByStartDesc(Long userId, LocalDateTime dateTime);

    List<Booking> findAllByBookerIdAndEndIsBeforeOrderByStartDesc(Long userId, LocalDateTime dateTime);

    List<Booking> findAllByBookerIdOrderByStartDesc(Long userId);

    @Query("SELECT b FROM Booking as b " +
            " JOIN Item as i ON b.item.id = i.id" +
            " WHERE i.owner.id = :ownerId " +
            " ORDER BY b.start DESC "
    )
    List<Booking> findAllByItemOwnerOrderByStartDesc(Long ownerId);

    @Query("SELECT b FROM Booking as b " +
            " JOIN Item as i ON b.item.id = i.id" +
            " WHERE i.owner.id = :ownerId " +
            " AND b.start >= :dateTime " +
            " ORDER BY b.start DESC "
    )
    List<Booking> findAllByItemOwnerAndStartIsAfterOrderByStartDesc(Long ownerId, LocalDateTime dateTime);

    @Query("SELECT b FROM Booking as b " +
            " JOIN Item as i ON b.item.id = i.id" +
            " WHERE i.owner.id = :ownerId " +
            " AND b.status = ru.practicum.shareit.booking.model.Status.REJECTED " +
            " ORDER BY b.start DESC "
    )
    List<Booking> findAllByItemOwnerAndStateRejectedOrderByStartDesc(Long ownerId);

    @Query("SELECT b FROM Booking as b " +
            " JOIN Item as i ON b.item.id = i.id" +
            " WHERE i.owner.id = :ownerId " +
            " AND b.status = ru.practicum.shareit.booking.model.Status.WAITING " +
            " ORDER BY b.start DESC "
    )
    List<Booking> findAllByItemOwnerAndStateWaitingOrderByStartDesc(Long ownerId);

    @Query("SELECT b FROM Booking as b " +
            " JOIN Item as i ON b.item.id = i.id" +
            " WHERE i.owner.id = :ownerId" +
            " AND b.start <= :dateTime " +
            " AND b.end >= :dateTime ")
    List<Booking> findAllByItemOwnerByDateIntoPeriodOrderByStartDesc(Long ownerId, LocalDateTime dateTime);

    @Query("SELECT b FROM Booking as b " +
            " JOIN Item as i ON b.item.id = i.id" +
            " WHERE i.owner.id = :ownerId" +
            " AND b.end < :dateTime " +
            " ORDER BY b.start DESC "
    )
    List<Booking> findAllByItemOwnerAndEndIsBeforeOrderByStartDesc(Long ownerId, LocalDateTime dateTime);

    @Query("SELECT b FROM Booking as b " +
            " WHERE b.item.id = :itemId " +
            " AND b.status = ru.practicum.shareit.booking.model.Status.APPROVED " +
            " AND b.start < :dateTime " +
            " ORDER BY b.start Desc "
    )
    List<Booking> findAllByItemIdAndOrderByStartDesc(Long itemId, LocalDateTime dateTime);

    @Query("SELECT b FROM Booking as b " +
            " WHERE b.item.id = :itemId " +
            " AND b.status = ru.practicum.shareit.booking.model.Status.APPROVED " +
            " AND b.start > :dateTime " +
            " ORDER BY b.start Asc "
    )
    List<Booking> findAllByItemIdOrderByStartAsc(Long itemId, LocalDateTime dateTime);

    @Query("SELECT b FROM Booking as b " +
            " JOIN Item as i ON b.item.id = i.id" +
            " WHERE i.id = :itemId " +
            " AND b.booker.id = :userId " +
            " AND b.status = ru.practicum.shareit.booking.model.Status.APPROVED " +
            " AND b.end < :dateTime " +
            " ORDER BY b.start DESC "
    )
    List<Booking> findAllByItemUserIdAndItemIdOrderByStartDesc(Long userId, Long itemId, LocalDateTime dateTime);

}


