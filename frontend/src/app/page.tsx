import Link from "next/link";
import styles from "./page.module.css";

export default function Home() {
  return (
    <main className={styles.container}>
      <section className={styles.hero}>
        <h1 className={styles.title}>
          Next.js Auth Starter
        </h1>

        <p className={styles.subtitle}>
          This is a base project with authentication, layout, and best practices
          ready to build new applications from scratch.
        </p>

        <div className={styles.actions}>
          <Link href="/login" className={styles.primaryBtn}>
            Go to Login
          </Link>

        </div>
      </section>
    </main>
  );
}
