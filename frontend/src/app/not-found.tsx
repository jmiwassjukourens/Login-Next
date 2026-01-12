"use client";

import { useRouter } from "next/navigation";
import styles from "./not-found.module.css";

export default function NotFound() {
  const router = useRouter();

  const goHome = () => {
    router.push("/");
  };

  const goBack = () => {
    router.back();
  };

  return (
    <div className={styles.notFoundContainer}>
      <div className={styles.content}>
        <h1 className={styles.errorCode}>404</h1>
        <h2 className={styles.errorMessage}>Page Not Found</h2>
        <p className={styles.errorDescription}>
          We&apos;re sorry, but the page you&apos;re looking for doesn&apos;t exist or has been moved.
        </p>

        <div className={styles.buttons}>
          <button
            className={`${styles.btn} ${styles.primary}`}
            onClick={goHome}
          >
            Go Home
          </button>

          <button
            className={`${styles.btn} ${styles.secondary}`}
            onClick={goBack}
          >
            Go Back
          </button>
        </div>
      </div>
    </div>
  );
}
