-- Create USERS table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL, -- Enum: stored as string
    last_seen BIGINT,
    enabled BOOLEAN,

    created_at INTEGER NOT NULL,
    updated_at INTEGER
    );

-- Indexes
CREATE INDEX IF NOT EXISTS idx_users_id ON users(id);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_created_at_desc ON users(created_at DESC);

-- Create ZOOMS table
CREATE TABLE IF NOT EXISTS zooms (
    id BIGSERIAL PRIMARY KEY,

    email VARCHAR(255),
    password VARCHAR(255),
    sdk_key VARCHAR(255),
    sdk_secret VARCHAR(255),
    api_key VARCHAR(255),
    api_secret VARCHAR(255),
    account_id VARCHAR(255),

    created_at INTEGER NOT NULL,
    updated_at INTEGER
    );

-- Indexes
CREATE INDEX IF NOT EXISTS idx_zooms_created_at ON zooms(created_at);

-- Create TRANSACTIONS table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    points BIGINT,
    description VARCHAR(255),
    is_credit BOOLEAN,
    created_at BIGINT,
    now BIGINT,
    after BIGINT
    );

-- Indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_transactions_user_created_at ON transactions(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_transactions_is_credit_created_at ON transactions(is_credit, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_transactions_user_credit ON transactions(user_id, is_credit);

-- Meeting table
CREATE TABLE IF NOT EXISTS meetings (
                                        id BIGSERIAL PRIMARY KEY,

                                        name VARCHAR(255),
    email VARCHAR(255),
    activated BOOLEAN DEFAULT FALSE,
    password VARCHAR(255),
    meeting_number VARCHAR(255) NOT NULL UNIQUE,
    meeting_password VARCHAR(255) NOT NULL,
    zoom_id BIGINT NOT NULL,
    created_at INT NOT NULL,
    updated_at INT,

    CONSTRAINT fk_meeting_zoom FOREIGN KEY (zoom_id) REFERENCES zooms(id) ON DELETE CASCADE
    );

-- Indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_meetings_created_at ON meetings(created_at);

-- Plans Table
CREATE TABLE IF NOT EXISTS plans (
                                     id BIGSERIAL PRIMARY KEY,

                                     name VARCHAR(255) NOT NULL,
    duration_in_months INT NOT NULL,
    duration_in_days INT NOT NULL,
    type VARCHAR(20) NOT NULL, -- PlanType enum ("FREE", "PAID")
    required_points INT NOT NULL,
    is_active BOOLEAN NOT NULL,

    created_at INT NOT NULL,
    updated_at INT
    );

-- Indexes for performance (optional depending on usage patterns)
CREATE INDEX IF NOT EXISTS idx_plans_type ON plans(type);
CREATE INDEX IF NOT EXISTS idx_plans_required_points ON plans(required_points);
CREATE INDEX IF NOT EXISTS idx_plans_is_active ON plans(is_active);

-- Refunds
CREATE TABLE IF NOT EXISTS refunds (
                                       id BIGSERIAL PRIMARY KEY,

                                       points INT NOT NULL,
                                       username VARCHAR(255) NOT NULL,
    userId BIGINT NOT NULL,
    parentId BIGINT NOT NULL,
    requester BIGINT NOT NULL,
    subscriptionName VARCHAR(255) NOT NULL,
    subscriptionStartedAt BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    refundingMonths INT NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,  -- Enum stored as string

    created_at INT NOT NULL,
    updated_at INT
    );

-- Indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_refunds_user_id ON refunds(userId);
CREATE INDEX IF NOT EXISTS idx_refunds_parent_id ON refunds(parentId);
CREATE INDEX IF NOT EXISTS idx_refunds_status ON refunds(status);
CREATE INDEX IF NOT EXISTS idx_refunds_created_at ON refunds(created_at);
CREATE INDEX IF NOT EXISTS idx_refunds_user_created_at ON refunds(userId, created_at DESC);


CREATE TABLE IF NOT EXISTS admins (
                                      id BIGINT PRIMARY KEY,  -- Also a FK to users.id
                                      points INT NOT NULL DEFAULT 0,
                                      CONSTRAINT fk_admin_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Create 'owners' table
CREATE TABLE IF NOT EXISTS owners (
                                      id BIGINT PRIMARY KEY,         -- Also FK to users.id
                                      points INT NOT NULL DEFAULT 0,
                                      admin_id BIGINT NOT NULL,

                                      CONSTRAINT fk_owner_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_owner_admin FOREIGN KEY (admin_id) REFERENCES admins(id) ON DELETE CASCADE
    );

-- Indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_owners_id ON owners(id);
CREATE INDEX IF NOT EXISTS idx_owners_admin_id ON owners(admin_id);
CREATE INDEX IF NOT EXISTS idx_owners_points ON owners(points);


-- Create 'supermasters' table
CREATE TABLE IF NOT EXISTS supermasters (
                                            id BIGINT PRIMARY KEY,  -- Same as users.id
                                            name VARCHAR(255),
    points INT NOT NULL DEFAULT 0,
    owner_id BIGINT NOT NULL,

    CONSTRAINT fk_supermaster_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_supermaster_owner FOREIGN KEY (owner_id) REFERENCES owners(id) ON DELETE CASCADE
    );

-- Indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_supermasters_owner_id ON supermasters(owner_id);
CREATE INDEX IF NOT EXISTS idx_supermasters_id ON supermasters(id);


CREATE TABLE IF NOT EXISTS masters (
    id BIGSERIAL PRIMARY KEY,
    points INT NOT NULL,
    lastRecharge BIGINT NOT NULL,
    parentType VARCHAR(50), -- RoleType enum stored as string

    parent_id BIGINT NOT NULL,

    created_at INT NOT NULL,
    updated_at INT
    );

-- Indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_masters_id ON masters(id);
CREATE INDEX IF NOT EXISTS idx_masters_parent_id ON masters(parent_id);
CREATE INDEX IF NOT EXISTS idx_masters_points ON masters(points);
CREATE INDEX IF NOT EXISTS idx_masters_last_recharge ON masters(lastRecharge);


CREATE TABLE IF NOT EXISTS subscribers (
                                           id BIGSERIAL PRIMARY KEY,

                                           startAt BIGINT NOT NULL,
                                           endAt BIGINT NOT NULL,
                                           lastRecharge BIGINT NOT NULL,
                                           canRefund BOOLEAN NOT NULL,
                                           refundableMonths INT NOT NULL,

                                           parentType VARCHAR(50), -- RoleType enum stored as string

    parent_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,

    created_at INT NOT NULL,
    updated_at INT
    );

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_subscribers_id ON subscribers(id);
CREATE INDEX IF NOT EXISTS idx_subscribers_parent_id ON subscribers(parent_id);
CREATE INDEX IF NOT EXISTS idx_subscribers_plan_id ON subscribers(plan_id);


CREATE TABLE IF NOT EXISTS managers (
                                        id BIGSERIAL PRIMARY KEY,

                                        parentType VARCHAR(50) NOT NULL, -- RoleType enum stored as string

    parent_id BIGINT NOT NULL,

    created_at INT NOT NULL,
    updated_at INT
    );

-- Indexes
CREATE INDEX IF NOT EXISTS idx_managers_id ON managers(id);
CREATE INDEX IF NOT EXISTS idx_managers_parent_id ON managers(parent_id);
CREATE INDEX IF NOT EXISTS idx_managers_role_type ON managers(parentType);





