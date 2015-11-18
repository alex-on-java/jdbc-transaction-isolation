### Sample project for transaction isolation in JDBC

For now, there are just 2 transactions with `READ_COMMITTED` isolation level.
They are working with the same data in the same way:
load data by id, get content, add a string to it and save the changed content.

The code shows, that if second transaction starts after first one and finishes before it,
it is waiting until the first transaction is committed.
Also it shows, that on `READ_COMMITTED` isolation level there could be easily a lost update situation.
If you change at least to `REPEATABLE_READ` level, the exception will raise on 2nd transaction commit attempt.