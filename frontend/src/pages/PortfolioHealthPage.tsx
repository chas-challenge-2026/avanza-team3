import AppCard from "../components/AppCard";
import styles from "./PortfolioHealthPage.module.css";
import { Gauge } from "@mui/x-charts/Gauge";
import DonutChart from "../components/DonutChart";
import useDashboard from "../hooks/useDasboard";
import InnehavsLista from "../components/Innehavslista";
import { useHoldings } from "../hooks/useHoldings";
import AllocationHandler from "../components/AllocationHandler";
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
import { useTargetAllocations } from "../hooks/useTargetAllocation";
import { mockPortfolioData } from "../data/portfolioMockData";
import InfoText from "../components/InfoText";
import Tabs from "@mui/material/Tabs";
import Tab from "@mui/material/Tab";
import Box from "@mui/material/Box";

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function PortfolioHealthPage() {
  const { dashboard, loading } = useDashboard();
  const { holdings } = useHoldings();
  const { riskData } = usePortfolioRisk();
  const {
    targetAllocation,
    editTargetAllocation,
    loading: targetAllocationLoading,
  } = useTargetAllocations();
  const [summaryHoldings, setSummaryHoldings] = useState<HoldingSummary[]>([]);
  const [value, setValue] = React.useState(0);

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

  const allocations = targetAllocation.map((target) => ({
    id: String(target.id),
    label: target.accountType,
    value: target.targetPercentage,
  }));

  const stockCount =
    summaryHoldings?.filter((holding) => holding.instrument_type === "Aktie")
      .length ?? 0;

  const fundCount =
    summaryHoldings?.filter((holding) => holding.instrument_type === "Fond")
      .length ?? 0;

  const getAccountInstrumentAllocations = (
    accountType: "ISK" | "KF" | "Depa",
  ) =>
    mockPortfolioData.accountTypeAssetClassAllocations.find(
      (allocation) => allocation.accountType === accountType,
    )?.assetClassAllocations ?? [];

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
              {stockCount} aktier · {fundCount} fonder
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
          {!loading && !targetAllocationLoading && dashboard && (
            <AllocationHandler
              allocations={allocations}
              title="Målallokering över kontotyp"
              onSave={async (values) => {
                await Promise.all(
                  targetAllocation.map((target) =>
                    editTargetAllocation(target.id, {
                      targetPercentage: values[String(target.id)],
                    }),
                  ),
                );
              }}
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
                data={mockPortfolioData.assetClassAllocations}
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
                data={getAccountInstrumentAllocations("ISK")}
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
                data={getAccountInstrumentAllocations("KF")}
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
                data={getAccountInstrumentAllocations("Depa")}
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
