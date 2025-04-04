PRAGMA journal_mode= WAL;
pragma synchronous = normal;
pragma temp_store = memory;
PRAGMA busy_timeout = 5000;

CREATE VIRTUAL TABLE tickers_search USING fts5(
    symbol,
    name
);

create table tickers
(
    symbol text primary key not null,
    name text not null,
    exchange text not null,
    exchangeName text not null,
    currency text not null,
    url text
);
