import { LazyMotion, domAnimation } from "framer-motion";
import { lazy, Suspense } from "react";
import Navbar from "./components/Navbar";
import Hero from "./components/Hero";
import Problem from "./components/Problem";
import Features from "./components/Features";
import Workflow from "./components/Workflow";
import SocialProof from "./components/SocialProof";
import Comparison from "./components/Comparison";
import Faq from "./components/Faq";
import FinalCta from "./components/FinalCta";
import Footer from "./components/Footer";

const Product = lazy(() => import("./components/Product"));

export default function App() {
  return (
    <LazyMotion features={domAnimation} strict>
      <div className="min-h-screen overflow-x-hidden">
        <Navbar />
        <main>
          <Hero />
          <Problem />
          <Features />
          <Suspense fallback={null}>
            <Product />
          </Suspense>
          <Workflow />
          <SocialProof />
          <Comparison />
          <Faq />
          <FinalCta />
        </main>
        <Footer />
      </div>
    </LazyMotion>
  );
}