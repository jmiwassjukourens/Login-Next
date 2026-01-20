"use client";

import { useState,useEffect } from "react";
import styles from "./login.module.css";
import { useAuth } from "@/app/context/AuthContext";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export default function LoginPage() {
  const { login } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  useEffect(() => {
    const init = async () => {
      try {
        await fetch(`${API_BASE_URL}/csrf`, {
          credentials: "include",
        });
      } catch (e) {
        console.error(e);
      }
    };
  
    init();
  }, []);

  return (
    <div className={styles.container}>
      <div className={`${styles.left} slide-left`}>
        {/*<Image
          src="/logo.png"
          alt="Company Logo"
          width={300}
          height={300}
          className={styles.logo}
          priority
        />*/}
      </div>

      <div className={`${styles.right} slide-right`}>
        <form
          className={styles.form}
          onSubmit={(e) => {
            e.preventDefault();
            login(username, password);
          }}
        >
          <h2 className={styles.title}>Log In</h2>

          <input
            className={styles.input}
            placeholder="User"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />

          <input
            type="password"
            className={styles.input}
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <button className={styles.button}>Submit</button>
        </form>
      </div>
    </div>
  );
}
