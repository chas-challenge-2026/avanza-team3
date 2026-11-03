import AppCard from "../components/AppCard";
import styles from "./PortfolioHealthPage.module.css";
import { Gauge } from "@mui/x-charts/Gauge";
import DonutChart from "../components/DonutChart";
import useDashboard from "../hooks/useDasboard";
import InnehavsLista from "../components/Innehavslista";
import { useHoldings } from "../hooks/useHoldings";
import NewAllocationHandler from "../components/NewAllocationHandler.tsx";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faHeart } from "@fortawesome/free-solid-svg-icons";
import CurrencyExposure from "../components/CurrencyExposure";
import PopOver from "../components/Popover";
import { usePortfolioRisk } from "../hooks/usePortfolioRisk";
import { calculateHoldingSpread } from "../utils/calculateHoldingSpread";
import React, { useState, useEffect } from "react";
import {
  getHoldingsSummary,
  type HoldingSummary,
} from "../services/holdingSummaryService";
import { useAccounts } from "../hooks/useAccounts";
import { useAccountAllocationTargets } from "../hooks/useAccountAllocationTargets";
import { replaceAccountAllocationTargets } from "../services/accountAllocationTargetsService";
import InfoText from "../components/InfoText";
import Tabs from "@mui/material/Tabs";
import Tab from "@mui/material/Tab";
import Box from "@mui/material/Box";
import { useAccountAllocations } from "../hooks/useAccountAllocations";

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function PortfolioHealthPage() {
  const { dashboard } = useDashboard();
  const { holdings } = useHoldings();
  const { riskData } = usePortfolioRisk();

  const [summaryHoldings, setSummaryHoldings] = useState<HoldingSummary[]>([]);
  const [value, setValue] = React.useState(0);

  const {
    accounts,
    loading: accountsLoading,
    error: accountsError,
  } = useAccounts();

  const accountIds = accounts.map((account) => account.id);

  const { allocations: accountAllocations } = useAccountAllocations(accountIds);

  const {
    targetsByAccount,
    loading: targetsLoading,
    error: targetsError,
  } = useAccountAllocationTargets(accountIds);

  const handleChange = (_event: React.SyntheticEvent, newValue: number) => {
    setValue(newValue);
  };

  useEffect(() => {
    const fetchSummary = async () => {
      try {
        const data = await getHoldingsSummary();
        setSummaryHoldings(data);
      } catch (error) {
        console.error("Kunde inte hämta holding summary:", error);
      }
    };

    fetchSummary();
  }, []);

  const statusColors = {
    good: "#16a34a",
    warning: "#d97706",
    danger: "#dc2626",
  };

  useEffect(() => {
    if (!dashboard) return;

    const holdings = dashboard.holdings.content;

    console.table(
      holdings.map((holding) => ({
        ticker: holding.ticker,
        valueSek: holding.valueSek,
        currency: holding.currency,
      })),
    );

    console.log(
      "Total SEK:",
      holdings.reduce((sum, holding) => sum + holding.valueSek, 0),
    );

    console.log("Spread:", calculateHoldingSpread(holdings));
  }, [dashboard]);

  const changePercent = 5;

  const trendStatus = () => {
    return changePercent >= 0 ? "good" : "danger";
  };

  const riskPoint = riskData?.current.riskScore;

  const riskLevel = () => {
    if (riskPoint === undefined) return "warning";

    if (riskPoint < 30) {
      return "good";
    } else if (riskPoint <= 60) {
      return "warning";
    } else {
      return "danger";
    }
  };

  const riskLevelText = {
    good: "Låg Risk",
    warning: "Måttlig Risk",
    danger: "Hög Risk",
  };

  const diversification = calculateHoldingSpread(
    summaryHoldings.map((holding) => ({
      ticker: holding.ticker,
      valueSek: holding.marketValue,
    })),
  );

  const diversificationLevel = () => {
    if (diversification >= 70) {
      return "good";
    } else if (diversification >= 40) {
      return "warning";
    } else {
      return "danger";
    }
  };

  const diversificationText = {
    good: "God spridning",
    warning: "Måttlig spridning",
    danger: "Låg spridning",
  };

  const riskHelp =
    "Riskpoäng sammanfattar hur mycket värdet på en investering kan svänga och hur stor risken är för förlust. Ju högre poäng, desto större svängningar kan du behöva tåla.";

  const spreadHelp =
    "Innehavsspridning visar hur jämnt portföljens värde är fördelat mellan olika innehav. Ett högre värde innebär att kapitalet är mer utspritt.";

  const allocations = accounts.map((account) => ({
    id: String(account.id),
    label: account.accountType,
    targets: targetsByAccount[account.id] ?? [],
  }));

  console.log(allocations);

  const stockCount = summaryHoldings.filter(
    (h) => h.instrument_type === "Aktie",
  ).length;

  const bondCount = summaryHoldings.filter(
    (h) => h.instrument_type === "Ränta",
  ).length;

  const otherCount = summaryHoldings.filter(
    (h) => h.instrument_type === "Övrigt",
  ).length;

  const categoryConfig: Record<string, { label: string; color: string }> = {
    STOCK: { label: "Aktier", color: "#3b82f6" },
    FUND: { label: "Fonder", color: "#22c55e" },
    BOND: { label: "Räntor", color: "#f59e0b" },
    OTHER: { label: "Övrigt", color: "#94a3b8" },
  };

  const toDonutData = (categories: { category: string; valueSek: number }[]) =>
    categories
      .filter((item) => item.valueSek > 0)
      .map((item) => ({
        label: categoryConfig[item.category]?.label ?? item.category,
        value: item.valueSek,
        color: categoryConfig[item.category]?.color ?? "#94a3b8",
      }));

  const getAccountDonutData = (accountType: string) => {
    const account = accounts.find((a) => a.accountType === accountType);

    const allocation = accountAllocations.find(
      (a) => a.accountId === account?.id,
    );

    return toDonutData(allocation?.categories ?? []);
  };

  const portfolioCategories = Object.keys(categoryConfig).map((category) => ({
    category,
    valueSek: accountAllocations.reduce(
      (sum, account) =>
        sum +
        (account.categories.find((item) => item.category === category)
          ?.valueSek ?? 0),
      0,
    ),
  }));

  const portfolioDonutData = toDonutData(portfolioCategories);

  const handleSaveAllocations = async (
    values: Record<string, [number, number]>,
  ) => {
    await Promise.all(
      accounts.map((account) => {
        const [stocks, stocksAndBonds] = values[String(account.id)];

        return replaceAccountAllocationTargets(account.id, {
          targets: [
            { category: "STOCK", targetPercentage: stocks },
            {
              category: "BOND",
              targetPercentage: stocksAndBonds - stocks,
            },
            {
              category: "OTHER",
              targetPercentage: 100 - stocksAndBonds,
            },
          ],
        });
      }),
    );
  };

  function CustomTabPanel(props: TabPanelProps) {
    const { children, value, index, ...other } = props;

    return (
      <div
        role="tabpanel"
        hidden={value !== index}
        tabIndex={0}
        id={`simple-tabpanel-${index}`}
        aria-labelledby={`simple-tab-${index}`}
        {...other}
      >
        {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
      </div>
    );
  }

  function a11yProps(index: number) {
    return {
      id: `simple-tab-${index}`,
      "aria-controls": `simple-tabpanel-${index}`,
    };
  }

  return (
    <>
      <div className={styles.container}>
        <div className={styles.titleRow}>
          <div className={styles.iconTitle}>
            <FontAwesomeIcon icon={faHeart} className={styles.icon} />
            <div className={styles.titleText}>
              <h1 className={styles.title}>Min Portföljhälsa</h1>
            </div>
          </div>
        </div>
      </div>
      <div className={styles.container}>
        <AppCard>
          <div className={styles.kpiCard + " " + styles[riskLevel()]}>
            <div className={styles.header}>
              <p className={styles.label}>Riskpoäng</p>
              <PopOver title="Vad betyder Riskpoäng?" content={riskHelp} />
            </div>
            <div className={styles.row}>
              <div>
                <p className={styles.value}>
                  {riskPoint !== undefined ? `${riskPoint}/100` : "–"}
                </p>
                <p className={styles.label + " " + styles[riskLevel()]}>
                  {riskPoint !== undefined
                    ? riskLevelText[riskLevel()]
                    : "Ingen data"}
                </p>
              </div>
              <Gauge
                sx={{
                  "& .MuiGauge-valueArc": { fill: statusColors[riskLevel()] },
                  "& .MuiGauge-valueText": { display: "none" },
                }}
                width={90}
                height={90}
                innerRadius="40%"
                outerRadius="80%"
                value={riskPoint}
              />
            </div>
          </div>
        </AppCard>
        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Totalt Portföljvärde</p>
            </div>
            <p className={styles.value}>
              {dashboard?.totalPortfolioValue.toLocaleString("sv-se", {
                maximumFractionDigits: 0,
              })}{" "}
              SEK
            </p>
            <p className={styles.label + " " + styles[trendStatus()]}>
              {changePercent >= 0 ? "+" : ""}
              {changePercent}% i år
            </p>
          </div>
        </AppCard>

        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Antal innehav</p>
            </div>
            <p className={styles.value}>{summaryHoldings?.length ?? "–"} st</p>

            <p className={styles.label}>
              {stockCount} aktier · {bondCount} räntor · {otherCount} övrigt
            </p>
          </div>
        </AppCard>
        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Innehavsspridning</p>
              <PopOver
                title="Vad betyder Innehavsspridning?"
                content={spreadHelp}
              />
            </div>
            <div className={styles.row}>
              <div>
                <p className={styles.value}>{diversification}/100</p>
                <p
                  className={
                    styles.label + " " + styles[diversificationLevel()]
                  }
                >
                  {diversificationText[diversificationLevel()]}
                </p>
              </div>
              <Gauge
                sx={{
                  "& .MuiGauge-valueArc": {
                    fill: statusColors[diversificationLevel()],
                  },
                  "& .MuiGauge-valueText": { display: "none" },
                }}
                width={90}
                height={60}
                value={diversification}
                startAngle={-90}
                endAngle={90}
                innerRadius="65%"
                outerRadius="100%"
              />
            </div>
          </div>
        </AppCard>
      </div>

      <div className={styles.infoRow}>
        <AppCard>
          <div className={styles.infoGrid}>
            <div>
              <h3 className={styles.infoTitle}>Vad betyder Riskpoäng?</h3>
              <InfoText content={riskHelp} />
            </div>
            <div>
              <h3 className={styles.infoTitle}>
                Vad betyder Innehavsspridning?
              </h3>
              <InfoText content={spreadHelp} />
            </div>
          </div>
        </AppCard>
      </div>

      <div className={styles.container}>
        <AppCard>
          <h2>Målallokering över kontotyp</h2>
          <p className={styles.allocationLabel}>
            Justera fördelning med reglagen för önskad fördelning över dina
            konton. <br />
          </p>
          {accountsLoading || targetsLoading ? (
            <p>Laddar konton och mål...</p>
          ) : accountsError || targetsError ? (
            <p role="alert">{accountsError ?? targetsError}</p>
          ) : (
            <NewAllocationHandler
              allocations={allocations}
              onSave={handleSaveAllocations}
            />
          )}
        </AppCard>
        <AppCard>
          <Box>
            <Box sx={{ borderBottom: 1, borderColor: "divider" }}>
              <Tabs
                value={value}
                onChange={handleChange}
                aria-label="Portföljfördelning per konto"
              >
                <Tab label="Portfölj" {...a11yProps(0)} />
                <Tab label="ISK" {...a11yProps(1)} />
                <Tab label="KF" {...a11yProps(2)} />
                <Tab label="Depå" {...a11yProps(3)} />
              </Tabs>
            </Box>
            <CustomTabPanel value={value} index={0}>
              <DonutChart
                help={{
                  title: "Vad betyder tillgångsslag?",
                  content:
                    "Tillgångsslag visar hur innehaven fördelas mellan aktier, räntebärande placeringar och övriga tillgångar.",
                }}
                title="Aktuell fördelning per tillgångsslag"
                data={portfolioDonutData}
              />
            </CustomTabPanel>
            <CustomTabPanel value={value} index={1}>
              <DonutChart
                help={{
                  title: "Vad betyder tillgångsslag?",
                  content:
                    "Tillgångsslag visar hur innehaven fördelas mellan aktier, räntebärande placeringar och övriga tillgångar.",
                }}
                title="Aktuell fördelning av aktier, räntor och övrigt i ISK"
                data={getAccountDonutData("ISK")}
              />
            </CustomTabPanel>
            <CustomTabPanel value={value} index={2}>
              <DonutChart
                help={{
                  title: "Vad betyder tillgångsslag?",
                  content:
                    "Tillgångsslag visar hur innehaven fördelas mellan aktier, räntebärande placeringar och övriga tillgångar.",
                }}
                title="Aktuell fördelning av aktier, räntor och övrigt i KF"
                data={getAccountDonutData("KF")}
              />
            </CustomTabPanel>
            <CustomTabPanel value={value} index={3}>
              <DonutChart
                help={{
                  title: "Vad betyder tillgångsslag?",
                  content:
                    "Tillgångsslag visar hur innehaven fördelas mellan aktier, räntebärande placeringar och övriga tillgångar.",
                }}
                title="Aktuell fördelning av aktier, räntor och övrigt i Depå"
                data={getAccountDonutData("Depa")}
              />
            </CustomTabPanel>
          </Box>
        </AppCard>
      </div>

      <div className={styles.container}>
        <CurrencyExposure />
      </div>

      <div className={styles.container}>
        <div className={styles.holdings}>
          <InnehavsLista
            holdings={holdings}
            columns={[
              "ticker",
              "instrumentName",
              "quantity",
              "avgBuyPrice",
              "valueSek",
              "account_type",
              "account_name",
              "marketValue",
              "pnl",
              "pnlPct",
            ]}
          />
        </div>
      </div>
    </>
  );
}

export default PortfolioHealthPage;
