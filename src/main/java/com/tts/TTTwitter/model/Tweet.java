package com.tts.TTTwitter.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.Length;

@Entity
public class Tweet {

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  @Column(name = "tweet_id")
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;
  
  @NotEmpty(message = "A tweet can't be blank")
  @Length(max = 280, message = "Tweet can't be more than 280 characters")
  private String message;
  
  @CreationTimestamp
  private Date createdAt;

  public Tweet() {}

  public Tweet(User user, String message, Date createdAt) {
    this.user = user;
    this.message = message;
    this.createdAt = createdAt;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  @Override
  public String toString() {
    return "Tweet [id=" + id + ", user=" + user
        + ", message=" + message + "]";
  }
  
}
