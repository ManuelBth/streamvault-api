-- V2: Remove is_kids column from profiles
ALTER TABLE profiles DROP COLUMN IF EXISTS is_kids;
