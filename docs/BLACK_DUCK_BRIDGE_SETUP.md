# Black Duck Bridge Setup Guide for Silverpeas Core

This document provides comprehensive instructions for setting up and using Black Duck Bridge scanning in the Silverpeas Core project.

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Required Secrets and Variables](#required-secrets-and-variables)
- [Workflow Configuration](#workflow-configuration)
- [Usage](#usage)
- [Troubleshooting](#troubleshooting)
- [Best Practices](#best-practices)

## Overview

Black Duck Bridge is a comprehensive security scanning tool that performs:
- **Software Composition Analysis (SCA)** - Identifies open source components and vulnerabilities
- **License Compliance** - Tracks license obligations and conflicts
- **Policy Enforcement** - Ensures compliance with organizational security policies
- **Vulnerability Management** - Provides detailed vulnerability reports and remediation guidance

The implemented solution provides three scanning modes:
1. **Full Scan** - Complete analysis for main branches
2. **Rapid Scan** - Fast analysis for pull requests
3. **Baseline Scan** - Comprehensive security assessment

## Prerequisites

### Black Duck Hub/Server Requirements
- Access to a Black Duck Hub/Server instance
- Valid Black Duck license with appropriate scanning quotas
- Network connectivity from GitHub Actions runners to Black Duck server
- Project permissions in Black Duck Hub

### Repository Requirements
- Admin access to the GitHub repository
- Ability to create and manage GitHub Secrets and Variables
- Maven project structure (already present in Silverpeas Core)

## Required Secrets and Variables

### GitHub Repository Variables

Navigate to `Settings > Secrets and variables > Actions > Variables` and add:

| Variable Name | Value | Description |
|---------------|-------|-------------|
| `BLACKDUCK_URL` | `https://your-blackduck-server.com` | Your Black Duck Hub/Server URL |

### GitHub Repository Secrets

Navigate to `Settings > Secrets and variables > Actions > Secrets` and add:

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `BLACKDUCK_TOKEN` | `your-api-token` | Black Duck API authentication token |

### How to Generate Black Duck API Token

1. Log into your Black Duck Hub/Server
2. Navigate to **User Profile** (top-right menu)
3. Go to the **Access Tokens** tab
4. Click **Create New Token**
5. Provide a name (e.g., "GitHub Actions - Silverpeas Core")
6. Set appropriate permissions:
   - **Project Creator** (if creating new projects)
   - **Project Manager** (for existing projects)
   - **Global Code Scanner** (recommended)
7. Copy the generated token immediately (it won't be shown again)
8. Add it as `BLACKDUCK_TOKEN` secret in GitHub

## Workflow Configuration

### Workflow Files

1. **`.github/workflows/black-duck-bridge.yml`** - Main workflow file
2. **`bridge-config.yml`** - Bridge configuration (optional but recommended)

### Workflow Triggers

The workflow is triggered on:
- **Push** to main branches (`main`, `master`, `develop`, `stage`, `release`)
- **Pull Request** to main branches (runs rapid scan)
- **Manual Trigger** via GitHub Actions UI with scan type selection

### Job Descriptions

#### 1. `black-duck-scan`
- **Trigger**: Push to main branches or manual dispatch
- **Duration**: ~30-60 minutes
- **Purpose**: Full security analysis
- **Outputs**: Complete vulnerability reports, SARIF files, policy violations

#### 2. `black-duck-pr-scan`
- **Trigger**: Pull requests
- **Duration**: ~15-30 minutes
- **Purpose**: Quick security check for PRs
- **Outputs**: Basic vulnerability scan, PR comment with results

#### 3. `security-baseline`
- **Trigger**: Manual dispatch on master branch
- **Duration**: ~60-120 minutes
- **Purpose**: Comprehensive security baseline
- **Outputs**: Extended reports including test dependencies

## Usage

### Running Scans

#### Automatic Scans
Scans run automatically on:
- Push to protected branches
- Pull request creation/updates

#### Manual Scans
1. Go to **Actions** tab in GitHub repository
2. Select **Black Duck Bridge Scan** workflow
3. Click **Run workflow**
4. Select scan type (FULL/RAPID/STATELESS)
5. Click **Run workflow**

### Viewing Results

#### GitHub Security Tab
- SARIF results are uploaded to GitHub Security
- View vulnerabilities in **Security > Code scanning alerts**

#### Workflow Artifacts
1. Go to completed workflow run
2. Download artifacts:
   - `blackduck-reports-{run-number}` - Contains all scan reports
   - PDF risk reports
   - JSON data files
   - Policy violation reports

#### Black Duck Hub
- Access detailed results in your Black Duck server
- Navigate to Projects > Silverpeas-Core
- View component inventory, vulnerability details, policy status

### Pull Request Integration

When a PR is created:
1. Rapid scan runs automatically
2. Results posted as PR comment
3. Blocking policy violations prevent merge
4. Non-blocking issues reported for awareness

## Troubleshooting

### Common Issues

#### 1. Authentication Failures
```
Error: Unable to connect to Black Duck server
```
**Solution**: 
- Verify `BLACKDUCK_URL` variable is correct
- Check `BLACKDUCK_TOKEN` secret is valid and not expired
- Ensure network connectivity between GitHub and Black Duck server

#### 2. Maven Build Failures
```
Error: Maven build failed during detect phase
```
**Solution**:
- Check Java version compatibility (using Java 21)
- Verify Maven dependencies are accessible
- Review Maven build command in workflow

#### 3. Timeout Issues
```
Error: Detect scan timed out
```
**Solution**:
- Increase timeout values in workflow
- Consider excluding test dependencies for faster scans
- Use rapid scan mode for quicker results

#### 4. Policy Violations
```
Error: Policy check failed - BLOCKER severity violations found
```
**Solution**:
- Review policy violations in Black Duck Hub
- Update/upgrade vulnerable components
- Request policy exception if needed
- Adjust policy severity settings if appropriate

### Debug Mode

To enable detailed logging:
1. Edit workflow file
2. Set `--detect-diagnostic-extended=true`
3. Review detailed logs in artifacts

### Support Resources

- **Black Duck Documentation**: [Black Duck Help](https://blackduck.docs.synopsys.com/)
- **Bridge CLI Documentation**: Available in Black Duck Hub
- **GitHub Actions Logs**: Check workflow run logs for detailed error messages

## Best Practices

### Security Practices

1. **Regular Scanning**: 
   - Run baseline scans weekly
   - Monitor security alerts promptly
   - Keep dependencies updated

2. **Policy Management**:
   - Define clear security policies
   - Set appropriate violation thresholds
   - Regular policy reviews

3. **Vulnerability Response**:
   - Prioritize CRITICAL and HIGH vulnerabilities
   - Track remediation progress
   - Document exceptions with business justification

### Performance Optimization

1. **Scan Efficiency**:
   - Use rapid scans for PRs
   - Exclude unnecessary file types
   - Optimize Maven build commands

2. **Resource Management**:
   - Monitor GitHub Actions usage
   - Adjust timeout values based on project size
   - Use caching for dependencies

3. **Artifact Management**:
   - Regular cleanup of old artifacts
   - Archive important baseline reports
   - Monitor storage usage

### Integration Practices

1. **CI/CD Integration**:
   - Block merges on critical vulnerabilities
   - Automate component updates where possible
   - Integrate with issue tracking

2. **Team Collaboration**:
   - Train team on vulnerability triage
   - Establish security review processes
   - Share security metrics and trends

3. **Compliance**:
   - Maintain audit trails
   - Document security decisions
   - Regular compliance reviews

## Configuration Customization

### Project-Specific Settings

Edit `bridge-config.yml` to customize:
- Excluded file patterns
- Maven scope settings
- Policy enforcement levels
- Timeout values

### Workflow Customization

Common modifications:
- Adjust trigger branches
- Modify scan frequency
- Change failure conditions
- Add notification steps

### Example Custom Policy
```yaml
detect:
  policy_check:
    fail_on_severities: "BLOCKER,CRITICAL"
    warn_on_severities: "MAJOR,MINOR"
    ignore_failure: false
```

## Maintenance

### Regular Tasks
- [ ] Review and update vulnerable dependencies monthly
- [ ] Audit policy violations quarterly
- [ ] Update Black Duck token before expiration
- [ ] Monitor scan performance and optimize as needed
- [ ] Review and update exclusion patterns as project evolves

### Monitoring
- Track scan execution times
- Monitor failure rates
- Review security metrics trends
- Assess policy compliance rates

---

For additional support, contact the Security Team or refer to the Black Duck Hub documentation.