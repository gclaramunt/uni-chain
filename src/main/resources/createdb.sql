  CREATE TABLE IF NOT EXISTS block (
      id   BIGINT,
      previous_hash VARBINARY,
      signature VARBINARY
    );
    CREATE TABLE IF NOT EXISTS transaction (
      source      VARCHAR,
      destination VARCHAR,
      amount NUMERIC,
      signature VARBINARY,
      hash VARBINARY,
      nonce   BIGINT,
      block_id BIGINT
    );