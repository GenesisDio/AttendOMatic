CREATE TABLE IF NOT EXISTS receipts (
  id INT NOT NULL AUTO_INCREMENT,
  student_id VARCHAR(255),
  time_submitted DATETIME,

  PRIMARY KEY (id)
);