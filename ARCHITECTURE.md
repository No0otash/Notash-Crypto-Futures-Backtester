# Architecture

UI -> BacktestViewModel -> CryptoBacktestEngine
                         |
                         +-> ExchangeAdapter
                         |    +-> CoinExAdapter
                         |    +-> BinanceAdapter
                         |    +-> BybitAdapter
                         |    +-> BitgetAdapter
                         |
                         +-> FundingEngine
                         +-> FeeEngine
                         +-> RiskEngine
                         +-> PerformanceEngine

The strategy is exchange-independent. Exchange adapters normalize exchange-specific
market data into Candle, FundingEvent and ContractSpec models.
