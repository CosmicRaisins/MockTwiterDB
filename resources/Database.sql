-- -----------------------------------------------------
-- Schema twitter
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `twitter` DEFAULT CHARACTER SET utf8 ;
USE `twitter` ;

-- -----------------------------------------------------
-- Table `twitter`.`followers`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `twitter`.`followers` (
  `user_id` BIGINT NOT NULL,
  `follows_id` BIGINT NOT NULL
  )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `twitter`.`tweets`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `twitter`.`tweets` (
  `tweet_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `tweet_ts` DATETIME NOT NULL,
  `tweet_text` VARCHAR(140) NOT NULL,
  PRIMARY KEY (`tweet_id`),
  UNIQUE INDEX `tweet_id_UNIQUE` (`tweet_id` ASC) VISIBLE,
  INDEX `user_id_idx` (`user_id` ASC) VISIBLE,
  CONSTRAINT `user_id`
    FOREIGN KEY (`user_id`)
    REFERENCES `twitter`.`followers` (`user_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;
