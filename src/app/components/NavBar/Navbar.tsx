"use client";

import Link from "next/link";
import { useState } from "react";
import { useAuth } from "@/app/context/AuthContext";
import styles from "./Navbar.module.css";

export default function Navbar() {
  const [openMenu, setOpenMenu] = useState<string | null>(null);
  const [mobileOpen, setMobileOpen] = useState(false);
  const { user, logout } = useAuth();

  const isLoggedIn = !!user;

  const toggleDropdown = (menu: string) => {
    setOpenMenu(openMenu === menu ? null : menu);
  };

  return (
    <nav className={styles.navbar}>
      <div className={styles.logo}>
        <Link href="/">App</Link>
      </div>

      <button
        className={styles.menuToggle}
        onClick={() => setMobileOpen(!mobileOpen)}
      >
        {mobileOpen ? "✖" : "☰"}
      </button>

      <ul className={`${styles.menu} ${mobileOpen ? styles.active : ""}`}>
        <li>
          <Link href="/" className={styles.menuItem}>
            Home
          </Link>
        </li>

        {/* Features */}
        <li className={styles.dropdownWrapper}>
          <button
            className={styles.menuItem}
            onClick={() => toggleDropdown("features")}
          >
            Features ▾
          </button>

          <ul
            className={`${styles.dropdown} ${
              openMenu === "features" ? styles.show : ""
            }`}
          >
            <li><Link href="/features/one">Feature One</Link></li>
            <li><Link href="/features/two">Feature Two</Link></li>
            <li><Link href="/features/three">Feature Three</Link></li>
          </ul>
        </li>

        {isLoggedIn ? (
          <li className={styles.dropdownWrapper}>
            <button
              className={styles.menuItem}
              onClick={() => toggleDropdown("account")}
            >
              Account ▾
            </button>

            <ul
              className={`${styles.dropdown} ${
                openMenu === "account" ? styles.show : ""
              }`}
            >
              <li><Link href="/dashboard">Dashboard</Link></li>
              <li><Link href="/profile">Profile</Link></li>
              <li>
                <button
                  className={styles.dropdownButton}
                  onClick={logout}
                >
                  Logout
                </button>
              </li>
            </ul>
          </li>
        ) : (
          <>
            <li>
              <Link href="/login" className={styles.menuItem}>Login</Link>
            </li>
            <li>
              <Link href="/register" className={styles.menuItem}>Register</Link>
            </li>
          </>
        )}
      </ul>
    </nav>
  );
}
